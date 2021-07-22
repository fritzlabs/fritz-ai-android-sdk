package ai.fritz.vision.video;

/**
 * Options used to configure exported video.
 */
public class ExportVideoOptions extends FrameProcessingOptions {

    private static final int DEFAULT_FRAME_RATE = 1;
    private static final int DEFAULT_KEYFRAME_INTERVAL = 0;
    private static final boolean DEFAULT_COPY_AUDIO = false;
    private static final int DEFAULT_BIT_RATE = Integer.MIN_VALUE;

    // Number of bits per second.
    // Affects the visual quality of the video.
    // Value will be automatically estimated if no value is assigned.
    public int bitRate;

    // Factor to scale the frame rate.
    // Affects the speed of the video.
    // The default value is 1.
    public double frameRateScale;

    // Frequency that key frames are requested per second.
    // Key frames are frames that contain full image data instead of transition data.
    // The higher the interval, the lower the resulting file size.
    // By default, all frames are key frames.
    public int keyFrameInterval;

    // If audio should also be exported.
    // Exporting audio will increase total export time and file size.
    // Audio will not be exported if frame rate is modified or the video
    // is not being exported from the beginning.
    // The default value is false.
    public boolean copyAudio;

    /**
     * Constructor using all default values.
     */
    public ExportVideoOptions() {
        super();
        this.bitRate = DEFAULT_BIT_RATE;
        this.frameRateScale = DEFAULT_FRAME_RATE;
        this.keyFrameInterval = DEFAULT_KEYFRAME_INTERVAL;
        this.copyAudio = DEFAULT_COPY_AUDIO;
    }

    /**
     * Constructor to set the frames to export with the copy audio option.
     *
     * @param frameInterval
     * @param numFrames
     * @param startFrameOffset
     * @param copyAudio
     */
    public ExportVideoOptions(int frameInterval, int numFrames, int startFrameOffset, boolean copyAudio) {
        this(frameInterval, numFrames, startFrameOffset,
                DEFAULT_BIT_RATE, DEFAULT_FRAME_RATE, DEFAULT_KEYFRAME_INTERVAL, copyAudio);
    }

    /**
     * Constructor to enable audio exporting.
     *
     * @param copyAudio
     */
    public ExportVideoOptions(boolean copyAudio) {
        this(DEFAULT_BIT_RATE, DEFAULT_FRAME_RATE, DEFAULT_KEYFRAME_INTERVAL, copyAudio);
    }

    /**
     * Constructor to specify each value individually.
     *
     * @param frameInterval
     * @param numFrames
     * @param startFrameOffset
     * @param bitRate
     * @param frameRateScale
     * @param keyFrameInterval
     * @param copyAudio
     */
    public ExportVideoOptions(int frameInterval, int numFrames, int startFrameOffset, int bitRate, int frameRateScale, int keyFrameInterval, boolean copyAudio) {
        super(frameInterval, numFrames, startFrameOffset);

        this.bitRate = bitRate;
        this.frameRateScale = frameRateScale;
        this.keyFrameInterval = keyFrameInterval;
        this.copyAudio = copyAudio;
    }

    @Override
    public void validate() {
        super.validate();

        if (frameRateScale <= 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "Frame rate scale must be greater than 0, was %f. ",
                            frameRateScale
                    )
            );
        }

        // TODO:
        //  Allow audio to be copied at an offset and when the frame rate is changed.
        if (copyAudio && (startingFrameOffset > 0 || frameRateScale != 1)) {
            throw new IllegalArgumentException(
                    String.format("Audio exporting not supported with startingFrameOffset = %f " +
                                    "and frameRateScale = %f. startingFrameOffset must be equal to 0 " +
                                    "and frameRateScale must be equal to 1.",
                            startingFrameOffset,
                            frameRateScale
                    )
            );
        }
    }
}
