package ai.fritz.vision.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

class CodecEncoder {

    private MediaCodec encoder;
    private TrackType trackType;
    private VideoTranscodeMediator mediator;
    private int encodeCount = 0;
    private boolean didStart = false;

    CodecEncoder(VideoTranscodeMediator mediator, TrackType trackType) {
        this.mediator = mediator;
        this.trackType = trackType;
    }

    /**
     * Start encoding process.
     *
     * @param outputFormat The format encoded data should conform to.
     * @param info Information used to find a proper device codec.
     */
    void startEncode(MediaFormat outputFormat, MediaCodecInfo info) {
        try {
            encoder = MediaCodec.createByCodecName(info.getName());
            encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            configureCallback();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid configuration for exporting video.");
        }
        encoder.start();
        didStart = true;
    }

    /**
     * Configures behavior for asynchronous video encoding.
     */
    private void configureCallback() {
        encoder.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(MediaCodec mediaCodec, int i) {
                DataSample frameSample = mediator.requestData(trackType);
                if (frameSample == null) {
                    // Queue an empty buffer instead of waiting for more samples
                    mediaCodec.queueInputBuffer(i, 0, 0, 0, 0);
                }
                else {
                    // Submit video data to be encoded
                    ByteBuffer inputBuffer = mediaCodec.getInputBuffer(i);
                    if (inputBuffer == null) {
                        throw new IllegalStateException("Input buffer does not exist.");
                    }
                    inputBuffer.put(frameSample.data);
                    inputBuffer.rewind();
                    mediaCodec.queueInputBuffer(i, 0, inputBuffer.remaining(), frameSample.timestampUs, 0);
                }
            }

            @Override
            public void onOutputBufferAvailable(MediaCodec mediaCodec, int i, MediaCodec.BufferInfo bufferInfo) {
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // Buffer contains initialization data instead of video data
                    mediaCodec.releaseOutputBuffer(i, false);
                    return;
                }
                boolean completed = false;
                if (bufferInfo.size > 0) {
                    encodeCount++;
                    ByteBuffer output = mediaCodec.getOutputBuffer(i);
                    completed = mediator.submitData(trackType, output, bufferInfo, encodeCount);
                }
                
                // Making sure encoder was not released after submitting data
                if (!completed) {
                    mediaCodec.releaseOutputBuffer(i, false);
                }
            }

            @Override
            public void onError(MediaCodec mediaCodec, MediaCodec.CodecException e) {
                throw new IllegalStateException(e);
            }

            @Override
            public void onOutputFormatChanged(MediaCodec mediaCodec, MediaFormat mediaFormat) {
                mediator.submitFormat(trackType, mediaFormat);
            }
        });
    }

    /**
     * Determines if the encoding process has started.
     *
     * @return the encoding state.
     */
    boolean encodingStarted() {
        return didStart;
    }

    /**
     * Retrieve the amount of frames that have currently been encoded.
     *
     * @return Encoded frame count.
     */
    int getEncodeCount() {
        return encodeCount;
    }

    /**
     * Release all resources.
     */
    void release() {
        if(encoder != null) {
            encoder.stop();
            encoder.release();
        }
    }
}
