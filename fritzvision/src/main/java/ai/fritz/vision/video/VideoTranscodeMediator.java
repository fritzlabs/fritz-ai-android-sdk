package ai.fritz.vision.video;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;
import java.util.Queue;

abstract class VideoTranscodeMediator<T> extends VideoDecodeMediator<T> {

    protected TrackDataMuxer dataSink;
    protected ExportVideoOptions options;
    protected TrackTypeMap<Queue<DataSample>> sampleMap = new TrackTypeMap<>();
    protected TrackTypeMap<CodecEncoder> encoderMap = new TrackTypeMap<>();

    VideoTranscodeMediator(
            int targetFrameCount,
            String outPath,
            FritzVisionImageFilter[] filters,
            ExportVideoOptions options
    ) {
        super(targetFrameCount, filters);
        this.dataSink = new TrackDataMuxer(outPath);
        this.options = options;
    }

    /**
     * Registers an encoder for a track type.
     *
     * @param type The track type.
     * @param encoder The encoder to register.
     */
    abstract void registerEncoder(TrackType type, CodecEncoder encoder);

    /**
     * Retrieve queued data.
     *
     * @param type Type of data to retrieve.
     * @return Data to be encoded.
     */
    abstract DataSample requestData(TrackType type);

    /**
     * Submit data to be muxed.
     *
     * @param type The track type.
     * @param buffer The data to mux.
     * @param info Information about the data.
     * @param totalSubmissions The amount of submitted data for the track.
     */
    abstract boolean submitData(TrackType type, ByteBuffer buffer, MediaCodec.BufferInfo info, int totalSubmissions);

    /**
     * Submit a format to configure the muxer.
     *
     * @param type The track type.
     * @param format The format used for configuration.
     */
    abstract void submitFormat(TrackType type, MediaFormat format);
}
