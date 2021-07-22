package ai.fritz.vision.video;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;

class CodecAudioDecoder extends CodecDecoder {

    public CodecAudioDecoder(VideoDecodeMediator mediator, MediaExtractor dataSource, MediaFormat inputFormat) {
        super(mediator, dataSource, inputFormat);
    }

    @Override
    void setupDecoder() {
        decoder.configure(inputFormat, null, null, 0);
    }

    @Override
    void drainDecoder(MediaCodec mediaCodec, int bufferIndex, long timestampUs) {
        ByteBuffer buffer = mediaCodec.getOutputBuffer(bufferIndex);
        if (buffer == null) {
            throw new IllegalStateException();
        }
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        mediator.transferData(data, timestampUs);

        mediaCodec.releaseOutputBuffer(bufferIndex, false);
    }

    @Override
    boolean isValidFrame(int frameInterval, int numFrames, int startFrame) {
        // TODO:
        //  Allow audio if starting from an offset.
        return startFrame == 0;
    }
}
