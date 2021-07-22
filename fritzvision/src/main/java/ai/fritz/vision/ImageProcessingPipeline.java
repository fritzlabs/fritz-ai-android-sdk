package ai.fritz.vision;

import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Size;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * The image processing pipeline uses a renderscript context in order to manipulate images
 * (resize, convert, rotate). This allows for faster image processing by multithreading work in
 * on threads or on the GPU.
 */
public class ImageProcessingPipeline {

    private static final String TAG = ImageProcessingPipeline.class.getSimpleName();
    private static final int kMaxChannelValue = 262143;

    private Allocation allocation;
    private int width;
    private int height;
    private Element element;

    public ImageProcessingPipeline(Bitmap bitmap) {
        this.allocation = Allocation.createFromBitmap(
                ProcessingContext.getInstance().getRS(),
                bitmap,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        this.element = allocation.getElement();
    }

    public ImageProcessingPipeline(YUVImage yuvImage) {
        this.allocation = convertYUVToRGB(yuvImage);
        this.width = yuvImage.getWidth();
        this.height = yuvImage.getHeight();
        this.element = allocation.getElement();
    }

    public ImageProcessingPipeline(ByteImage byteImage) {
        this.allocation = convertFromBytes(byteImage);
        this.width = byteImage.getWidth();
        this.height = byteImage.getHeight();
        this.element = allocation.getElement();
    }

    public ImageProcessingPipeline(Allocation allocation, int width, int height) {
        this.allocation = allocation;
        this.width = width;
        this.height = height;
        this.element = allocation.getElement();
    }

    public Allocation getAllocation() {
        return allocation;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Bitmap buildBitmap() {
        Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        allocation.copyTo(outputBitmap);
        return outputBitmap;
    }

    public ByteImage buildByteImage() {
        byte[] destination = new byte[width * height * ByteImage.RGB_CHANNELS];
        allocation.copyTo(destination);
        return new ByteImage(destination, width, height);
    }

    public ByteImage buildYuvByteImage() {
        byte[] destination = new byte[width * height * ByteImage.RGB_CHANNELS];
        allocation.copyTo(destination);
        return new ByteImage(convertRGBAToYUV420(destination), width, height);
    }

    /**
     * Orient the image using an {@link ImageOrientation}.
     *
     * @param orientation
     */
    public void orient(ImageOrientation orientation) {
        if (orientation.rotation > 0f) {
            // Rotate target allocation
            rotate(orientation.rotation);
        }

        if (orientation.getFlipVertical()) {
            flipVertical();
        }

        if (orientation.getFlipHorizontal()) {
            flipHorizontal();
        }
    }

    /**
     * Rotates the image.
     *
     * @param rotation The degree to rotate.
     */
    public void rotate(int rotation) {
        allocation = ProcessingContext.getInstance().rotate(allocation, element, width, height, rotation);
        switch (rotation) {
            case 90:
            case 270: {
                int tmpWidth = width;
                width = height;
                height = tmpWidth;
                break;
            }
        }
    }

    /**
     * Resizes the image.
     *
     * @param targetSize The dimensions to resize to.
     */
    public void resize(Size targetSize) {
        allocation = ProcessingContext.getInstance().resize(allocation, element, targetSize);
        width = targetSize.getWidth();
        height = targetSize.getHeight();
    }

    /**
     * Flip image horizontally the image.
     */
    public void flipHorizontal() {
        allocation = ProcessingContext.getInstance().flipHorizontal(allocation, element, width, height);
    }

    /**
     * Flip image vertically the image.
     */
    public void flipVertical() {
        allocation = ProcessingContext.getInstance().flipVertical(allocation, element, width, height);
    }

    /**
     * Convert a byte array into an allocation.
     *
     * @param byteImage The byte image data.
     * @return An allocation from the array.
     */
    private Allocation convertFromBytes(ByteImage byteImage) {
        return createAllocation(byteImage.getCopyOfImageData(), byteImage.getWidth(), byteImage.getHeight());
    }

    /**
     * Convert YUV4208888 to RGB
     * <p>
     * Couldn't quite get the ScriptIntrinsicYuvToRGB to work with this format.
     * It was mainly converting NV21 to RGB.
     *
     * @param yuvImage The source YUV image.
     * @return An allocation from the converted image.
     */
    private Allocation convertYUVToRGB(YUVImage yuvImage) {
        byte[] argb = convertYUV420ToARGB8888(
                yuvImage.getY(),
                yuvImage.getU(),
                yuvImage.getV(),
                yuvImage.getWidth(),
                yuvImage.getHeight(),
                yuvImage.getYRowStride(),
                yuvImage.getUVRowStride(),
                yuvImage.getUVPixelStride());
        return createAllocation(argb, yuvImage.getWidth(), yuvImage.getHeight());
    }

    /**
     * Configure and create an allocation from a byte array.
     *
     * @param source The source byte array.
     * @param width The width of the image in the byte array.
     * @param height The height of the image in the byte array.
     * @return A configured allocation.
     */
    private Allocation createAllocation(byte[] source, int width, int height) {
        RenderScript rs = ProcessingContext.getInstance().getRS();
        Type outType = Type.createXY(rs, Element.RGBA_8888(rs), width, height);
        Allocation allocation = Allocation.createTyped(rs, outType, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        allocation.copyFrom(source);

        return allocation;
    }

    private byte[] convertRGBAToYUV420(byte[] rgba) {
        byte[] yuv = new byte[width * height * 3 / 2];
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int scanline = 0;
        int pixelCount = 0;

        int B, G, R, Y, V, U;
        for (int i = 0; i < rgba.length; i += ByteImage.RGB_CHANNELS) {

            B = rgba[i] & 0xFF;
            G = rgba[i + 1] & 0xFF;
            R = rgba[i + 2] & 0xFF;

            Y = ((66 * B + 129 * G + 25 * R + 128) >> 8) + 16;
            yuv[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));

            // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2.
            // So, for every 4 Y pixels there are 1 V and 1 U. Note the sampling is every other
            // pixel AND every other scan line.
            if (pixelCount % 2 == 0 && scanline % 2 == 0) {
                V = ((-38 * B - 74 * G + 112 * R + 128) >> 8) + 128;
                U = ((112 * B - 94 * G - 18 * R + 128) >> 8) + 128;
                yuv[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                yuv[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
            }
            pixelCount++;
            if (yIndex >= width && yIndex % width == 0) scanline++;
        }

        return yuv;
    }

    // Conversion modified from:
    // https://github.com/tensorflow/examples/blob/master/lite/examples/image_classification/android/app/src/main/java/org/tensorflow/lite/examples/classification/env/ImageUtils.java
    private byte[] convertYUV420ToARGB8888(
            byte[] yData,
            byte[] uData,
            byte[] vData,
            int width,
            int height,
            int yRowStride,
            int uvRowStride,
            int uvPixelStride) {
        byte[] argb = new byte[4 * width * height];

        int yp = 0;
        for (int j = 0; j < height; j++) {
            int pY = yRowStride * j;
            int pUV = uvRowStride * (j >> 1);

            for (int i = 0; i < width; i++) {
                int uv_offset = pUV + (i >> 1) * uvPixelStride;

                int y = 0xff & yData[pY + i];
                int u = 0xff & uData[uv_offset];
                int v = 0xff & vData[uv_offset];

                // Adjust and check YUV values
                y = (y - 16) < 0 ? 0 : (y - 16);
                u -= 128;
                v -= 128;

                // This is the floating point equivalent. We do the conversion in integer
                // because some Android devices do not have floating point in hardware.
                // nR = (int)(1.164 * nY + 2.018 * nU);
                // nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
                // nB = (int)(1.164 * nY + 1.596 * nV);
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                // Clipping RGB values to be inside boundaries [ 0 , kMaxChannelValue ]
                r = r > kMaxChannelValue ? kMaxChannelValue : (r < 0 ? 0 : r);
                g = g > kMaxChannelValue ? kMaxChannelValue : (g < 0 ? 0 : g);
                b = b > kMaxChannelValue ? kMaxChannelValue : (b < 0 ? 0 : b);

                int pixel = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);

                int alpha = (pixel >> 24) & 0xff;
                int red   = (pixel >> 16) & 0xff;
                int green = (pixel >>  8) & 0xff;
                int blue  = (pixel      ) & 0xff;

                argb[yp++] = (byte) red;
                argb[yp++] = (byte) green;
                argb[yp++] = (byte) blue;
                argb[yp++] = (byte) alpha;
            }
        }

        return argb;
    }
}
