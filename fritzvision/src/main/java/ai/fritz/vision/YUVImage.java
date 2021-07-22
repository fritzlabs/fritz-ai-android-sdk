package ai.fritz.vision;

import android.media.Image;

import java.nio.ByteBuffer;

public class YUVImage {

    private int width;
    private int height;
    private byte[] y;
    private byte[] u;
    private byte[] v;
    private int yRowStride;
    private int uvRowStride;
    private int uvPixelStride;

    public YUVImage(byte[] y, byte[] u, byte[] v, int yRowStride, int uvRowStride, int uvPixelStride, int width, int height) {
        this.width = width;
        this.height = height;
        this.y = y;
        this.u = u;
        this.v = v;
        this.yRowStride = yRowStride;
        this.uvRowStride = uvRowStride;
        this.uvPixelStride = uvPixelStride;
    }

    public YUVImage(Image image) {
        width = image.getWidth();
        height = image.getHeight();

        // Get the three image planes
        Image.Plane[] planes = image.getPlanes();

        // Get strides
        yRowStride = planes[0].getRowStride();
        uvRowStride = planes[1].getRowStride();  // We know from documentation that RowStride is the same for U and V.
        uvPixelStride = planes[1].getPixelStride();  // We know from documentation that PixelStride is the same for U and V.

        ByteBuffer buffer = planes[0].getBuffer();
        y = new byte[buffer.remaining()];
        buffer.get(y);
        // Must rewind otherwise this will mutate the Image state.
        buffer.rewind();

        buffer = planes[1].getBuffer();
        u = new byte[buffer.remaining()];
        buffer.get(u);
        buffer.rewind();

        buffer = planes[2].getBuffer();
        v = new byte[buffer.remaining()];
        buffer.get(v);
        buffer.rewind();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getY() {
        return y;
    }

    public byte[] getU() {
        return u;
    }

    public byte[] getV() {
        return v;
    }

    public int getYRowStride() {
        return yRowStride;
    }

    public int getUVRowStride() {
        return uvRowStride;
    }

    public int getUVPixelStride() {
        return uvPixelStride;
    }
}
