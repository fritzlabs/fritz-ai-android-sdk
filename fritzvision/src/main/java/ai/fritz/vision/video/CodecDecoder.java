package ai.fritz.vision.video;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Objects;

abstract class CodecDecoder {

    protected MediaCodec decoder;
    protected MediaExtractor dataSource;
    protected MediaFormat inputFormat;
    protected VideoDecodeMediator mediator;
    protected int decodeCount = 0;
    protected boolean didStart = false;

    CodecDecoder(VideoDecodeMediator mediator, MediaExtractor dataSource, MediaFormat inputFormat) {
        this.mediator = mediator;
        this.dataSource = dataSource;
        this.inputFormat = inputFormat;
    }

    /**
     * Start decoding process.
     *
     * @param frameInterval Interval to get frames.
     * @param numFrames Number of frames to get.
     * @param startFrame Frame to start at.
     */
    void startDecode(int frameInterval, int numFrames, int startFrame) {
        try {
            decoder = MediaCodec.createDecoderByType(
                    Objects.requireNonNull(inputFormat.getString(MediaFormat.KEY_MIME))
            );
            setupDecoder();
            configureCallback(frameInterval, numFrames, startFrame);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to start processing video.");
        }
        decoder.start();
        didStart = true;
    }

    /**
     * Initialize the decoder.
     */
    abstract void setupDecoder();

    /**
     * Determines if a frame should be rendered to a surface.
     *
     * @param frameInterval Interval to get frames.
     * @param numFrames Number of frames to get.
     * @param startFrame Frame to start at.
     * @return If the frame is valid.
     */
    abstract boolean isValidFrame(int frameInterval, int numFrames, int startFrame);


    /**
     * Drain a single buffer.
     * The buffer must be released after processing.
     *
     * @param mediaCodec The codec to drain.
     * @param bufferIndex The index to release the buffer at.
     * @param timestampUs Timestamp of the data.
     */
    abstract void drainDecoder(
            MediaCodec mediaCodec,
            int bufferIndex,
            long timestampUs
    );

    /**
     * Configures behavior for asynchronous video decoding.
     *
     * @param frameInterval Interval to get frames.
     * @param numFrames Number of frames to get.
     * @param startFrame Frame to start at.
     */
    private void configureCallback(final int frameInterval, final int numFrames, final int startFrame) {
        decoder.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(MediaCodec mediaCodec, int i) {
                // Signal that decoding should end when no more data is available
                if (dataSource.getSampleTrackIndex() < 0) {
                    mediaCodec.queueInputBuffer(
                            i,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    );
                    return;
                }

                // Submit raw data to be decoded
                ByteBuffer buffer = mediaCodec.getInputBuffer(i);
                if (buffer == null) {
                    throw new IllegalStateException("Output buffer is invalid.");
                }

                int byteCount = dataSource.readSampleData(buffer, 0);
                if (byteCount > 0) {
                    mediaCodec.queueInputBuffer(
                            i,
                            0,
                            byteCount,
                            dataSource.getSampleTime(),
                            dataSource.getSampleFlags()
                    );
                }
                dataSource.advance();
            }

            @Override
            public void onOutputBufferAvailable(MediaCodec mediaCodec, int i, MediaCodec.BufferInfo bufferInfo) {
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // Buffer contains initialization data instead of video data
                    mediaCodec.releaseOutputBuffer(i, false);
                    return;
                }
                boolean render = bufferInfo.size > 0 && isValidFrame(frameInterval, numFrames, startFrame);
                decodeCount++;
                if (render) {
                    drainDecoder(mediaCodec, i, bufferInfo.presentationTimeUs);
                }
                else {
                    mediaCodec.releaseOutputBuffer(i, false);
                }
            }

            @Override
            public void onError(MediaCodec mediaCodec, MediaCodec.CodecException e) {
                throw new IllegalStateException(e);
            }

            @Override
            public void onOutputFormatChanged(MediaCodec mediaCodec, MediaFormat mediaFormat) {

            }
        });
    }

    /**
     * Determines if the decoding process has started.
     *
     * @return The decoding state.
     */
    boolean decodingStarted() {
        return didStart;
    }

    /**
     * Retrieve the amount of frames that have currently been decoded.
     *
     * @return Decoded frame count.
     */
    int getDecodeCount() {
        return decodeCount;
    }

    /**
     * Retrieve the input format for the video.
     *
     * @return The video input format.
     */
    MediaFormat getInputFormat() {
        return inputFormat;
    }

    /**
     * Release all resources.
     */
    void release() {
        if (decoder != null) {
            decoder.stop();
            decoder.release();
        }
        dataSource.release();
    }
}
