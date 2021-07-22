package ai.fritz.vision.video;

/**
 * Sets parameters used for processing a specific set of video frames.
 */
public class FrameProcessingOptions {

    private static final int DEFAULT_INTERVAL = 1;
    private static final int DEFAULT_LENGTH = Integer.MAX_VALUE;
    private static final int DEFAULT_START_OFFSET = 0;

    // The interval to fetch frames
    public int frameInterval;

    // The number of frames to get.
    public int numFrames;

    // The starting frame offset.
    public int startingFrameOffset;

    public FrameProcessingOptions() {
        this.frameInterval = DEFAULT_INTERVAL;
        this.numFrames = DEFAULT_LENGTH;
        this.startingFrameOffset = DEFAULT_START_OFFSET;
    }

    public FrameProcessingOptions(int frameInterval, int numFrames, int startingFrameOffset) {
        this.frameInterval = frameInterval;
        this.numFrames = numFrames;
        this.startingFrameOffset = startingFrameOffset;
    }

    public void validate() {
        if (frameInterval <= 0) {
            throw new IllegalArgumentException(String.format(
                    "Frame interval must be greater than 0, was %d.",
                    frameInterval
            ));
        }

        if (numFrames <= 0) {
            throw new IllegalArgumentException(String.format(
                    "Number of frames must be greater than 0, was %d.",
                    numFrames
            ));
        }

        if (startingFrameOffset < 0) {
            throw new IllegalArgumentException(String.format(
                    "Starting frame must be at least 0, was %d.",
                    startingFrameOffset
            ));
        }
    }
}
