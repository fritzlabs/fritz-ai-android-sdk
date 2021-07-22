package ai.fritz.vision.video;

import android.os.Handler;
import android.os.HandlerThread;

import ai.fritz.vision.ByteImage;

abstract class VideoDecodeMediator<T> {

    private static final String THREAD_NAME = "Fritz Vision Video Processing Thread";

    protected int targetFrameCount;
    protected FritzVisionImageFilter[] filters;
    protected VideoProgressCallback<T> progressCallback;
    protected HandlerThread handlerThread;
    protected TrackTypeMap<CodecDecoder> decoderMap = new TrackTypeMap<>();

    VideoDecodeMediator(int targetFrameCount, FritzVisionImageFilter[] filters) {
        this.targetFrameCount = targetFrameCount;
        this.filters = filters;
    }

    /**
     * Process and queue image data.
     *
     * @param image Image to process.
     * @param timestampUs Timestamp for the image.
     */
    abstract void transferData(ByteImage image, long timestampUs);

    /**
     * Process and queue raw byte data.
     *
     * @param data Data to process.
     * @param timestampUs Timestamp for the data.
     */
    abstract void transferData(byte[] data, long timestampUs);

    /**
     * Start decoding process.
     *
     * @param frameInterval Interval to get frames.
     * @param numFrames Number of frames to get.
     * @param startFrame Frame to start at.
     */
    void start(final int frameInterval, final int numFrames, final int startFrame) {
        handlerThread = new HandlerThread(THREAD_NAME);
        handlerThread.start();

        // Start decoding video on a separate thread
        new Handler(handlerThread.getLooper()).post(new Runnable() {
            @Override
            public void run() {
                for (CodecDecoder decoder : decoderMap.values()) {
                    decoder.startDecode(frameInterval, numFrames, startFrame);
                }
            }
        });
    }

    /**
     * Release all resources.
     */
    void release() {
        for (CodecDecoder decoder : decoderMap.values()) {
            decoder.release();
        }
        handlerThread.quitSafely();
    }

    /**
     * Registers a decoder for a track type.
     *
     * @param type The track type.
     * @param decoder The decoder to register.
     */
    void registerDecoder(TrackType type, CodecDecoder decoder) {
        decoderMap.set(type, decoder);
    }

    /**
     * Sets a callback to handle decode or encode progress.
     *
     * @param progressCallback The listener.
     */
    void setListener(VideoProgressCallback<T> progressCallback) {
        this.progressCallback = progressCallback;
    }
}
