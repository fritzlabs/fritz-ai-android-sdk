package ai.fritz.vision.video;

import android.net.Uri;

import ai.fritz.vision.FritzVisionImage;

public class FritzVisionVideo {

    private DataSourceFactory factory;
    private FritzVisionImageFilter[] filters;

    /**
     * Initializes a video from a file path without filters.
     * Path must be local and conform to a URI file scheme.
     *
     * @param filePath Path to a video.
     */
    public FritzVisionVideo(String filePath) {
        this(filePath, new FritzVisionImageFilter[0]);
    }

    /**
     * Initializes a video from a file path with a single filter.
     * Path must be local and conform to a URI file scheme.
     *
     * @param filePath Path to a video.
     * @param filter The filter to apply on the video.
     */
    public FritzVisionVideo(String filePath, FritzVisionImageFilter filter) {
        this(filePath, new FritzVisionImageFilter[]{filter});
    }

    /**
     * Initializes a video from a file path with filters.
     * Path must be local and conform to a URI file scheme.
     *
     * @param filePath Path to a video.
     * @param filters The filters to apply on the video.
     */
    public FritzVisionVideo(String filePath, FritzVisionImageFilter[] filters) {
        if (filters == null) {
            throw new IllegalArgumentException("Filters for FritzVisionVideo must not be null.");
        }
        this.filters = filters;
        this.factory = new DataSourceFactory(filePath);
    }

    /**
     * Initializes a video from an URI without filters.
     * URI can point to a file locally or over the cloud.
     *
     * @param uri Path to video.
     */
    public FritzVisionVideo(Uri uri) {
        this(uri, new FritzVisionImageFilter[0]);
    }

    /**
     * Initializes a video from an URI with a single filter.
     * URI can point to a file locally or in the cloud.
     *
     * @param uri Path to video.
     * @param filter The filter to apply on the video.
     */
    public FritzVisionVideo(Uri uri, FritzVisionImageFilter filter) {
        this(uri, new FritzVisionImageFilter[]{filter});
    }

    /**
     * Initializes a video from an URI with filters.
     * URI can point to a file locally or in the cloud.
     *
     * @param uri Path to video.
     * @param filters The filters to apply on the video.
     */
    public FritzVisionVideo(Uri uri, FritzVisionImageFilter[] filters) {
        if (filters == null) {
            throw new IllegalArgumentException("Filters for FritzVisionVideo must not be null.");
        }
        this.filters = filters;
        this.factory = new DataSourceFactory(uri);
    }

    /**
     * Asynchronously get frames of the video, one a a time, with all filters applied.
     * Callback is called on a background thread upon each frame extraction.
     *
     * @param frameCallback Callback to receive frames.
     */
    public void getFrames(FrameProcessingOptions options, final FrameProgressCallback frameCallback) {
        options.validate();

        int frameInterval = options.frameInterval;
        int numFrames = options.numFrames;
        int startFrame = options.startingFrameOffset;
        validateBounds(frameInterval + startFrame);

        int adjustedLength = Math.min(numFrames, factory.getTotalFrameCount());
        int targetFrames = factory.getFramesToProcessCount(frameInterval, adjustedLength, startFrame);
        VideoDecodeMediator<FritzVisionImage> mediator = new VideoExtractionMediator(targetFrames, filters);
        CodecDecoder videoDecoder = new CodecVideoDecoder(
                mediator,
                factory.popExtractor(TrackType.VIDEO),
                factory.getTrackFormat(TrackType.VIDEO)
        );
        mediator.registerDecoder(TrackType.VIDEO, videoDecoder);
        mediator.setListener(frameCallback);
        mediator.start(frameInterval, adjustedLength, startFrame);
    }

    /**
     * Asynchronously export a video to a file path with all filters applied.
     * The exported video will be configured using default options.
     * Callback is called on a background thread upon each file write.
     *
     * @param outPath Path to export the video to.
     * @param exportListener Listener to track export progress.
     */
    public void export(String outPath, final ExportProgressCallback exportListener) {
        export(outPath, new ExportVideoOptions(), exportListener);
    }

    /**
     * Asynchronously export a video to a file path with all filters applied.
     * The exported video will be configured according to the specified options.
     * Callback is called on a background thread upon each file write.
     *
     * @param outPath Path to export the video to.
     * @param options Configurations for the exported video.
     * @param exportListener Listener to track export progress.
     */
    public void export(
            String outPath,
            ExportVideoOptions options,
            final ExportProgressCallback exportListener
    ) {
        options.validate();
        int frameInterval = options.frameInterval;
        int numFrames = options.numFrames;
        int startFrame = options.startingFrameOffset;
        validateBounds(frameInterval + startFrame);

        int totalFrames = getTotalFrameCount();
        int adjustedLength = Math.min(numFrames, totalFrames);
        int targetFrames = factory.getFramesToProcessCount(frameInterval, adjustedLength, startFrame);
        VideoTranscodeMediator<Float> mediator;
        if (options.copyAudio) {
            factory.prepare(TrackType.AUDIO);

            // Calculate the timestamp to stop encoding audio at
            int framesToDecode = Math.min((adjustedLength * frameInterval) + startFrame, totalFrames);
            long targetDuration = factory.frameToTimestamp(framesToDecode);
            mediator = new VideoExportMediator(targetFrames, targetDuration, outPath, filters, options);

            // Create and register the decoder and encoder for audio
            CodecDecoder audioDecoder = new CodecAudioDecoder(
                    mediator,
                    factory.popExtractor(TrackType.AUDIO),
                    factory.getTrackFormat(TrackType.AUDIO)
            );
            CodecEncoder audioEncoder = new CodecEncoder(mediator, TrackType.AUDIO);
            mediator.registerDecoder(TrackType.AUDIO, audioDecoder);
            mediator.registerEncoder(TrackType.AUDIO, audioEncoder);
        }
        else {
            if (startFrame > 0) {
                // Adjust the offset to a new key frame, if any
                startFrame -= factory.seekTracksToFrame(startFrame);
            }
            mediator = new VideoExportMediator(targetFrames, outPath, filters, options);
        }

        // Create and register the decoder and encoder for video
        CodecDecoder videoDecoder = new CodecVideoDecoder(
                mediator,
                factory.popExtractor(TrackType.VIDEO),
                factory.getTrackFormat(TrackType.VIDEO)
        );
        CodecEncoder videoEncoder = new CodecEncoder(mediator, TrackType.VIDEO);
        mediator.registerDecoder(TrackType.VIDEO, videoDecoder);
        mediator.registerEncoder(TrackType.VIDEO, videoEncoder);
        mediator.setListener(exportListener);
        mediator.start(frameInterval, adjustedLength, startFrame);
    }

    /**
     * Retrieve the duration of the video in microseconds.
     *
     * @return Video duration.
     */
    public long getDurationUs() {
        if (!factory.isTrackPrepared(TrackType.VIDEO)) {
            factory.prepare(TrackType.VIDEO);
        }
        return factory.getDurationInMicroseconds();
    }

    /**
     * Retrieve the total amount of frames in the video.
     *
     * @return Total frame count.
     */
    public int getTotalFrameCount() {
        if (!factory.isTrackPrepared(TrackType.VIDEO)) {
            factory.prepare(TrackType.VIDEO);
        }
        return factory.getTotalFrameCount();
    }

    public int getTotalAudioSampleCount() {
        if (!factory.isTrackPrepared(TrackType.AUDIO)) {
            factory.prepare(TrackType.AUDIO);
        }
        return factory.getTotalAudioSampleCount();
    }

    /**
     * Retrieve the frame rate of the video.
     *
     * @return Video frame rate.
     */
    public int getFrameRate() {
        if (!factory.isTrackPrepared(TrackType.VIDEO)) {
            factory.prepare(TrackType.VIDEO);
        }
        return factory.getDataRate(TrackType.VIDEO);
    }

    /**
     * Determines if the processing bounds are valid.
     *
     * @param minFrames Minimum number of frames to decode.
     */
    private void validateBounds(int minFrames) {
        int totalFrames = getTotalFrameCount();
        if (minFrames >= totalFrames) {
            throw new IllegalArgumentException(String.format(
                    "Sum of frame interval and starting frame must be less than " +
                            "the total frame count, %d, was %d.",
                            totalFrames,
                            minFrames
                    )
            );
        }
    }

    /**
     * Callback for progress when exporting.
     * Responses are received on a background thread.
     * If making changes to a View, make sure to run those changes on the UI thread.
     */
    public interface ExportProgressCallback extends VideoProgressCallback<Float> {
    }

    /**
     * Callback for frames when extracting frames.
     * Responses are received on a background thread.
     * If making changes to a View, make sure to run those changes on the UI thread.
     */
    public interface FrameProgressCallback extends VideoProgressCallback<FritzVisionImage> {
    }
}
