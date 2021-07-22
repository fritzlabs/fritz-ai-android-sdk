package ai.fritz.vision.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import ai.fritz.vision.ByteImage;
import ai.fritz.vision.FritzVisionImage;

class VideoExportMediator extends VideoTranscodeMediator<Float> {

    private int currentFrameCount = 0;
    private long currentAudioTimestamp = 0;
    private long targetDuration = 1;

    private boolean videoReady = true;
    private boolean audioReady = true;

    VideoExportMediator(
            int targetFrameCount,
            String outPath,
            FritzVisionImageFilter[] filters,
            ExportVideoOptions options
    ) {
        super(targetFrameCount, outPath, filters, options);
    }

    VideoExportMediator(
            int targetFrameCount,
            long targetDuration,
            String outPath,
            FritzVisionImageFilter[] filters,
            ExportVideoOptions options
    ) {
        super(targetFrameCount, outPath, filters, options);
        this.targetDuration = targetDuration;
    }

    @Override
    void transferData(ByteImage image, long timestampUs) {
        FritzVisionImage visionImage = FritzVisionImage.applyingFilters(image, filters);

        // Initialize the queue for video if it does not exist yet.
        // Encoding process starts here to ensure that the dimensions for the encoder format
        // matches the dimensions of the predicted image.
        if (!sampleMap.hasVideo()) {
            sampleMap.setVideo(new LinkedBlockingQueue<DataSample>());
            if (decoderMap.hasVideo() && encoderMap.hasVideo()) {
                CodecDecoder decoder = decoderMap.getVideo();
                CodecEncoder encoder = encoderMap.getVideo();
                MediaFormat outputFormat = EncoderFormatFactory.getVideoFormat(
                        visionImage.getWidth(),
                        visionImage.getHeight(),
                        decoder.getInputFormat(),
                        options
                );
                MediaCodecInfo info = EncoderFormatFactory.getVideoInfo();
                encoder.startEncode(outputFormat, info);
            }
            else {
                throw new IllegalStateException("A decoder must have a matching encoder.");
            }
        }
        DataSample sample = createSample(visionImage.buildOrientedYuvByteImage().getCopyOfImageData(), timestampUs);
        sampleMap.getVideo().add(sample);
    }

    @Override
    void transferData(byte[] data, long timestampUs) {
        // Initialize the queue for audio if it does not exist yet.
        if (!sampleMap.hasAudio()) {
            sampleMap.setAudio(new LinkedBlockingQueue<DataSample>());
            if (decoderMap.hasAudio() && encoderMap.hasAudio()) {
                CodecDecoder decoder = decoderMap.getAudio();
                CodecEncoder encoder = encoderMap.getAudio();
                MediaFormat outputFormat = EncoderFormatFactory.getAudioFormat(decoder.getInputFormat());
                MediaCodecInfo info = EncoderFormatFactory.getAudioInfo();
                encoder.startEncode(outputFormat, info);
            }
            else {
                throw new IllegalStateException("A decoder must have a matching encoder.");
            }
        }
        DataSample sample = createSample(data, timestampUs);
        sampleMap.getAudio().add(sample);
    }

    @Override
    DataSample requestData(TrackType type) {
        if (!sampleMap.has(type)) {
            return null;
        }
        return sampleMap.get(type).poll();
    }

    @Override
    boolean submitData(TrackType type, ByteBuffer data, MediaCodec.BufferInfo info, int totalSubmissions) {
        // Make sure extra data is not transcoded
        switch (type) {
            case VIDEO:
                // Check frame count for video since 1 decoded frame maps to 1 viewable frame
                if (totalSubmissions > targetFrameCount) return false;
                currentFrameCount = totalSubmissions;
                break;
            case AUDIO:
                // Check duration for audio since 1 decoded audio frame may play for a
                // fraction of a viewable frame or for multiple viewable frames
                if (currentAudioTimestamp > targetDuration) return false;
                currentAudioTimestamp = info.presentationTimeUs;
                break;
        }
        dataSink.writeData(type, data, info);
        float frameCompletion = currentFrameCount / (float) (targetFrameCount * sampleMap.size());
        float audioCompletion = currentAudioTimestamp / (float) (targetDuration * sampleMap.size());

        // Round to the nearest hundredth and make sure the total does not exceed 1
        float roundedCompletion = Math.round((frameCompletion + audioCompletion) * 100f) / 100f;
        float completionRate = Math.min(1, roundedCompletion);
        if (progressCallback != null) {
            progressCallback.onProgress(completionRate);
        }

        // Release resources and finalize export when all data is transcoded
        boolean didComplete = (completionRate == 1);
        if (didComplete) {
            release();
            if (progressCallback != null) {
                progressCallback.onComplete();
            }
        }
        return didComplete;
    }

    @Override
    void submitFormat(TrackType type, MediaFormat format) {
        // Encoder has been been properly formatted for its track
        switch (type) {
            case VIDEO:
                videoReady = true;
                break;
            case AUDIO:
                audioReady = true;
                break;
        }
        dataSink.addTrack(type, format);
        if (videoReady && audioReady) {
            // Start muxing when all tracks are ready
            dataSink.startMuxing();
        }
    }

    @Override
    void registerEncoder(TrackType type, CodecEncoder encoder) {
        // New tracks have unformatted encoders
        switch (type) {
            case VIDEO:
                videoReady = false;
                break;
            case AUDIO:
                audioReady = false;
                break;
        }
        encoderMap.set(type, encoder);
    }

    @Override
    void release() {
        for (CodecEncoder encoder : encoderMap.values()) {
            encoder.release();
        }
        super.release();
        dataSink.release();
    }

    /**
     * Creates a sample that can queued.
     * Scales the time to change frame rate.
     *
     * @param data The raw byte data.
     * @param timestampUs The timestamp of the data.
     * @return A sample.
     */
    private DataSample createSample(byte[] data, long timestampUs) {
        DataSample sample = new DataSample();
        sample.data = data;
        sample.timestampUs = (long) (timestampUs / options.frameRateScale);
        return sample;
    }
}
