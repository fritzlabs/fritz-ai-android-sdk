package ai.fritz.vision;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;

/**
 * A byte representation of an image.
 */
public class ByteImage {

    public static final int RGB_CHANNELS = 4;
    private byte[] imageData;
    private int width;
    private int height;

    public ByteImage(byte[] imageData, int width, int height) {
        this.imageData = imageData;
        this.width = width;
        this.height = height;
    }

    public ByteImage(ByteBuffer buffer, int width, int height) {
        this(buffer, width, height, RGB_CHANNELS);
    }

    public ByteImage(ByteBuffer buffer, int width, int height, int channels) {
        byte[] imageData = new byte[width * height * channels];
        buffer.get(imageData);
        this.imageData = imageData;
        this.width = width;
        this.height = height;
    }

    /**
     * Retrieve a copy of the image data.
     *
     * @return The bytes forming the image.
     */
    public byte[] getCopyOfImageData() {
        return Arrays.copyOf(imageData, imageData.length);
    }

    /**
     * Retrieve the image data wrapped in a ByteBuffer.
     *
     * @return The image data in a ByteBuffer.
     */
    public ByteBuffer getByteBuffer() {
        return ByteBuffer.wrap(imageData);
    }

    /**
     * Convert the image data to integers and order it in an IntBuffer.
     *
     * @return The image data in a IntBuffer with native endianness.
     */
    public IntBuffer getIntBuffer() {
        return getByteBuffer().order(ByteOrder.nativeOrder()).asIntBuffer();
    }

    /**
     * Retrieve the width of the original image.
     *
     * @return Image width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Retrieve the height of the original image.
     *
     * @return Image height.
     */
    public int getHeight() {
        return height;
    }
}
