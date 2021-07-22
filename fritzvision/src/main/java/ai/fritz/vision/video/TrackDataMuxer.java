package ai.fritz.vision.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Handles muxing of data into video.
 */
class TrackDataMuxer {

    private MediaMuxer muxer;
    private boolean didStart;
    private int videoOutputTrack = -1;
    private int audioOutputTrack = -1;

    TrackDataMuxer(String outPath) {
        try {
            this.muxer = new MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Add a track to write data to.
     *
     * @param type The type of track (video/audio).
     * @param format The output format for the track.
     */
    void addTrack(TrackType type, MediaFormat format) {
        switch (type) {
            case VIDEO:
                videoOutputTrack = muxer.addTrack(format);
                break;
            case AUDIO:
                audioOutputTrack = muxer.addTrack(format);
                break;
        }
    }

    /**
     * Initiate video muxing.
     */
    void startMuxing() {
        if (!didStart) {
            muxer.start();
            didStart = true;
        }
    }

    /**
     * Mux a video frame.
     *
     * @param buffer The frame data.
     * @param info The frame information.
     */
    void writeData(TrackType type, ByteBuffer buffer, MediaCodec.BufferInfo info) {
        if (didStart) {
            switch (type) {
                case VIDEO:
                    muxer.writeSampleData(videoOutputTrack, buffer, info);
                    break;
                case AUDIO:
                    muxer.writeSampleData(audioOutputTrack, buffer, info);
                    break;
            }
        }
    }

    /**
     * Determines if the muxing process has started.
     *
     * @return The muxing state.
     */
    boolean muxingStarted() {
        return didStart;
    }

    /**
     * Release all resources.
     *
     * @throws IllegalStateException When 0 audio or video buffers are encoded.
     */
    void release() {
        try {
            muxer.stop();
            muxer.release();
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("Not enough audio or video to process. " +
                    "Try increasing the number of frames.");
        }
    }
}
