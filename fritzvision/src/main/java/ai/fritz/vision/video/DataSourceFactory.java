package ai.fritz.vision.video;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;

import java.io.IOException;

import ai.fritz.core.Fritz;

/**
 * Handles querying of video data.
 * Coordinates video and audio tracks.
 * Encapsulates math for determining video timestamps and frame numbers.
 */
class DataSourceFactory {

    private abstract class SourceManager {

        /**
         * Applies a data source to an extractor.
         *
         * @param extractor The extractor to set a data source.
         * @throws IOException If the source is invalid.
         */
        abstract void applySource(MediaExtractor extractor) throws IOException;
    }

    private class UriSourceManager extends SourceManager {
        private Uri uri;

        UriSourceManager(Uri uri) {
            this.uri = uri;
        }

        @Override
        void applySource(MediaExtractor extractor) throws IOException {
            extractor.setDataSource(Fritz.getAppContext(), uri, null);
        }
    }

    private class FilePathSourceManager extends SourceManager {
        private String filePath;

        FilePathSourceManager(String filePath) {
            this.filePath = filePath;
        }

        @Override
        void applySource(MediaExtractor extractor) throws IOException {
            extractor.setDataSource(filePath);
        }
    }

    private final float US_SCALE = 1000000;
    private final String VIDEO_TYPE = "video/";
    private final String AUDIO_TYPE = "audio/";

    private MediaExtractor videoSource;
    private MediaExtractor audioSource;

    private MediaFormat videoFormat;
    private MediaFormat audioFormat;

    private SourceManager manager;

    DataSourceFactory(String filePath) {
        this.manager = new FilePathSourceManager(filePath);
    }

    DataSourceFactory(Uri uri) {
        this.manager = new UriSourceManager(uri);
    }

    /**
     * Selects the type of track.
     *
     * @param extractor The source of the tracks.
     * @param type The track type.
     * @return The format of the selected track.
     */
    private MediaFormat selectTrack(MediaExtractor extractor, String type) {
        MediaFormat format;
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith(type)) {
                extractor.selectTrack(i);
                return format;
            }
        }
        throw new IllegalArgumentException("A track type does not exist.");
    }

    /**
     * Prepares the data source matching the specified type.
     *
     * @param type The track type.
     * @return This for chaining.
     */
    DataSourceFactory prepare(TrackType type) {
        switch (type) {
            case VIDEO:
                try {
                    videoSource = new MediaExtractor();
                    manager.applySource(videoSource);
                    videoFormat = selectTrack(videoSource, VIDEO_TYPE);
                } catch (IOException e) {
                    throw new IllegalStateException("Invalid file path.");
                }
                break;
            case AUDIO:
                try {
                    audioSource = new MediaExtractor();
                    manager.applySource(audioSource);
                    audioFormat = selectTrack(audioSource, AUDIO_TYPE);
                } catch (IOException e) {
                    throw new IllegalStateException("Invalid file path.");
                }
                break;
        }
        return this;
    }

    /**
     * Retrieves the data source matching the specified type.
     * The selected data source is removed to ensure that an already played or modified
     * data source does not persist for future calls.
     *
     * @param type The track type.
     * @return The data source.
     */
    MediaExtractor popExtractor(TrackType type) {
        MediaExtractor extractor = null;
        switch (type) {
            case VIDEO:
                if (videoSource == null) {
                    throw new IllegalArgumentException("Video track not found.");
                }
                extractor = videoSource;
                videoSource = null;
                break;
            case AUDIO:
                if (audioSource == null) {
                    throw new IllegalArgumentException("Audio track not found.");
                }
                extractor = audioSource;
                audioSource = null;
        }
        return extractor;
    }

    /**
     * Retrieve the format of the specified track.
     *
     * @param type The track type.
     * @return The track format.
     */
    MediaFormat getTrackFormat(TrackType type) {
        switch (type) {
            case VIDEO:
                return videoFormat;
            case AUDIO:
                return audioFormat;
            default:
                throw new IllegalArgumentException("Invalid track type.");
        }
    }

    /**
     * Seek both audio and video tracks to the nearest key frame.
     *
     * @param frame The frame to seek to.
     * @return The frame sought to.
     */
    int seekTracksToFrame(int frame) {
        if (videoSource == null) {
            throw new IllegalStateException("Video track must be prepared to seek.");
        }
        int keyFrame = 0;
        long sampleTime;
        while ((sampleTime = videoSource.getSampleTime()) != -1) {
            if ((videoSource.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_SYNC) > 0) {
                int currentFrame = timestampToFrame(sampleTime);
                if (keyFrame == currentFrame || frame < currentFrame) break;
                keyFrame = currentFrame;
            }

            // Theoretically, seeking to the current time + 1 should move to the next key frame.
            // For some reason it doesn't, so seek to the current time + a large number.
            videoSource.seekTo(sampleTime + 1000, MediaExtractor.SEEK_TO_NEXT_SYNC);
        }

        long time = frameToTimestamp(keyFrame);
        videoSource.seekTo(time, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        if (audioSource != null) {
            audioSource.seekTo(time, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        }
        return keyFrame;
    }

    /**
     * Retrieve the data rate of the track corresponding to the specified track.
     * Units are frames/second for video and samples/second for audio.
     *
     * @param type The track type.
     * @return The data rate.
     */
    int getDataRate(TrackType type) {
        switch (type) {
            case VIDEO:
                if (videoFormat == null) {
                    throw new IllegalArgumentException("Video track not found.");
                }
                return videoFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
            case AUDIO:
                if (audioFormat == null) {
                    throw new IllegalArgumentException("Audio track not found.");
                }
                return audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            default:
                throw new IllegalArgumentException("Invalid track type.");
        }
    }

    /**
     * Retrieve the total amount of frames in the video.
     *
     * @return Total frame count.
     */
    int getTotalFrameCount() {
        if (videoFormat == null) {
            throw new IllegalArgumentException("Video track not found.");
        }
        // Frames per second * length of the video in seconds
        return (int) (getDataRate(TrackType.VIDEO) * (getDurationInMicroseconds() / US_SCALE));
    }

    /**
     * Retrieve the audio sample count in the video.
     *
     * @return the total audio sample count.
     */
    int getTotalAudioSampleCount() {
        if (audioFormat == null) {
            throw new IllegalArgumentException("Audio track not found.");
        }
        // audio samples per second * length of the video in seconds
        return (int) (getDataRate(TrackType.AUDIO) * (getDurationInMicroseconds() / US_SCALE));
    }

    /**
     * Calculate the total amount of frames to process.
     *
     * @param frameInterval Extraction interval.
     * @param numFrames Number of frames to extract.
     * @param startFrame Frame to start extraction.
     * @return The total amount of frames.
     */
    int getFramesToProcessCount(int frameInterval, int numFrames, int startFrame) {
        if (videoSource == null) {
            throw new IllegalStateException("Video track must be prepared to calculate bounded data.");
        }
        int totalFrames = getTotalFrameCount();

        // Get the actual number of frames to be decoded
        int offsetLength = numFrames + startFrame > totalFrames
                ? Math.abs(numFrames - startFrame)
                : numFrames;
        int offsetFrames = Math.abs(totalFrames - startFrame);

        boolean withinBounds = offsetFrames > offsetLength * frameInterval;
        if (!withinBounds) {
            // If the desired number of frames exceeds the frames in the video due to the interval,
            // round to the nearest multiple of the interval and find the greatest common factor
            // between the rounded frames in the video and the interval
            return (offsetFrames - (offsetFrames % frameInterval)) / frameInterval;
        }
        if (frameInterval < offsetLength) {
            // If the interval is less than the desired number of frames,
            // round to the nearest multiple of the interval
            return (offsetLength - (offsetLength % frameInterval));
        }
        return offsetLength;
    }

    /**
     * Retrieve the duration of the track.
     *
     * @return The track duration.
     */
    long getDurationInMicroseconds() {
        if (videoFormat != null) {
            return videoFormat.getLong(MediaFormat.KEY_DURATION);
        }
        if (audioFormat != null) {
            return audioFormat.getLong(MediaFormat.KEY_DURATION);
        }
        throw new IllegalStateException("No track formats found.");
    }

    /**
     * Determines if a specified data source is attached.
     *
     * @param type The track type.
     * @return If the data source is attached.
     */
    boolean isTrackPrepared(TrackType type) {
        switch (type) {
            case VIDEO:
                return videoSource != null;
            case AUDIO:
                return audioSource != null;
            default:
                throw new IllegalArgumentException("Invalid track type");
        }
    }

    /**
     * Converts a frame number into its corresponding timestamp.
     *
     * @param frame The frame number to convert.
     * @return The timestamp.
     */
    long frameToTimestamp(int frame) {
        int frameRate = getDataRate(TrackType.VIDEO);
        return (long) (frame * US_SCALE) / frameRate;
    }

    /**
     * Converts a timestamp into its corresponding frame number.
     *
     * @param timestamp The timestamp to convert.
     * @return The frame number.
     */
    int timestampToFrame(long timestamp) {
        int frameRate = getDataRate(TrackType.VIDEO);
        return (int) (frameRate * (timestamp / US_SCALE));
    }
}
