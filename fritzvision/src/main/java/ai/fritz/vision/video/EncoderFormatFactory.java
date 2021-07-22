package ai.fritz.vision.video;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

/**
 * Creates necessary data for an encoder.
 *
 * @hide
 */
class EncoderFormatFactory {

    private static final String VIDEO_MIME_TYPE = "video/avc";
    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";

    /**
     * Setup the output format for video.
     *
     * @param width The width of the video.
     * @param height The height of the video.
     * @param originalFormat The original video format of the video.
     * @param options The options for the video.
     * @return Video output format.
     */
    static MediaFormat getVideoFormat(int width, int height, MediaFormat originalFormat, ExportVideoOptions options) {
        int outFrameRate = (int) (originalFormat.getInteger(MediaFormat.KEY_FRAME_RATE) * options.frameRateScale);
        int outBitRate = (int) (options.bitRate <= 0
                ? estimateVideoBitRate(width, height, outFrameRate)
                : options.bitRate
        );
        MediaFormat videoFormat = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, width, height);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, outBitRate);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, outFrameRate);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, options.keyFrameInterval);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        return videoFormat;
    }

    /**
     * Retrieve information needed to encode video data.
     *
     * @return Video information.
     */
    static MediaCodecInfo getVideoInfo() {
        return setupInfo(VIDEO_MIME_TYPE);
    }

    /**
     * Setup the output format for audio.
     *
     * @param originalFormat The original audio format of the video.
     * @return Audio output format.
     */
    static MediaFormat getAudioFormat(MediaFormat originalFormat) {
        MediaFormat audioFormat = MediaFormat.createAudioFormat(
                AUDIO_MIME_TYPE,
                originalFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                originalFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        );
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, originalFormat.getInteger(MediaFormat.KEY_BIT_RATE));
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        return audioFormat;
    }

    /**
     * Retrieve information needed to encode audio data.
     *
     * @return Audio information.
     */
    static MediaCodecInfo getAudioInfo() {
        return setupInfo(AUDIO_MIME_TYPE);
    }

    /**
     * Setup information needed to encode data into a video format.
     */
    private static MediaCodecInfo setupInfo(String mime) {
        MediaCodecList codecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        for (MediaCodecInfo codecInfo : codecList.getCodecInfos()) {
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(mime)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    /**
     * Estimates the bit rate for a video.
     * Should be a reasonable default for AVC.
     * https://stackoverflow.com/a/5220554/4288782
     *
     * @param width Width of the video.
     * @param height Height of the video.
     * @param frameRate Frame rate of the video.
     * @return The estimated bit rate of the video.
     */
    private static float estimateVideoBitRate(int width, int height, int frameRate) {
        return 0.07f * 2 * width * height * frameRate;
    }
}
