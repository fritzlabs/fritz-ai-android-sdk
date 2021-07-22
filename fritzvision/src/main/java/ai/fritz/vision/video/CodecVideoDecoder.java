package ai.fritz.vision.video;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.GLES20;

import java.nio.ByteBuffer;

import ai.fritz.vision.ByteImage;

/**
 * Handles decoding of video.
 */
class CodecVideoDecoder extends CodecDecoder {

    private DecoderOutputSurface outputSurface;
    private ByteBuffer pixelData;
    private int videoWidth;
    private int videoHeight;

    public CodecVideoDecoder(VideoDecodeMediator mediator, MediaExtractor dataSource, MediaFormat inputFormat) {
        super(mediator, dataSource, inputFormat);
        int width = inputFormat.getInteger(MediaFormat.KEY_WIDTH);
        int height = inputFormat.getInteger(MediaFormat.KEY_HEIGHT);
        this.videoWidth = Math.min(width, height);
        this.videoHeight = Math.max(width, height);
        this.pixelData = ByteBuffer.allocate(videoWidth * videoHeight * ByteImage.RGB_CHANNELS);
    }

    @Override
    void setupDecoder() {
        outputSurface = new DecoderOutputSurface(videoWidth, videoHeight);
        outputSurface.releaseEglContext();
        decoder.configure(inputFormat, outputSurface.getSurface(), null, 0);
    }

    @Override
    void drainDecoder(MediaCodec mediaCodec, int bufferIndex, long timestampUs) {
        // Write buffer data to the surface
        mediaCodec.releaseOutputBuffer(bufferIndex, true);
        outputSurface.makeCurrent();
        outputSurface.awaitNewImage();
        outputSurface.drawImage();
        mediator.transferData(readSurface(), timestampUs);
    }

    @Override
    boolean isValidFrame(int frameInterval, int numFrames, int startFrame) {
        // Check that frame is within bounds
        if (decodeCount < startFrame
                || decodeCount >= (numFrames + startFrame) * frameInterval) {
            return false;
        }
        // Check that frame is in the proper interval
        return decodeCount % frameInterval == 0;
    }

    /**
     * Read pixels currently on the surface.
     *
     * @return The pixels on the surface.
     */
    private ByteImage readSurface() {
        pixelData.clear();
        GLES20.glReadPixels(
                0,
                0,
                videoWidth,
                videoHeight,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                pixelData
        );
        flipPixels();
        return new ByteImage(pixelData, videoWidth, videoHeight);
    }

    /**
     * Flip the current pixel data.
     * Pixels read from OpenGL are initially upside-down since its coordinates start in the
     * bottom-left corner instead of the top-left corner.
     */
    private void flipPixels() {
        pixelData.rewind();
        int rowLength = videoWidth * ByteImage.RGB_CHANNELS;
        byte[] tmp = new byte[rowLength];
        for (int i = 0; i < videoHeight / 2; i++) {
            pixelData.get(tmp);
            System.arraycopy(
                    pixelData.array(),
                    pixelData.limit() - pixelData.position(),
                    pixelData.array(),
                    pixelData.position() - rowLength,
                    rowLength
            );
            System.arraycopy(
                    tmp,
                    0,
                    pixelData.array(),
                    pixelData.limit() - pixelData.position(),
                    rowLength
            );
        }
        pixelData.rewind();
    }

    /**
     * Release all resources.
     */
    @Override
    void release() {
        super.release();
        if (outputSurface != null) {
            outputSurface.release();
        }
    }
}
