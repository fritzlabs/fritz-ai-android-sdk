package ai.fritz.vision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.media.Image;
import android.renderscript.Allocation;
import android.util.Base64;
import android.util.Log;
import android.util.Size;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import ai.fritz.core.Fritz;
import ai.fritz.core.annotations.Base64EncodableImage;
import ai.fritz.vision.imagesegmentation.BlendMode;
import ai.fritz.vision.poseestimation.Pose;
import ai.fritz.vision.video.FritzVisionImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSoftLightBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageTwoInputFilter;

/**
 * FritzVisionImage is a standard input class for FritzVisionPredictors.
 */
public class FritzVisionImage implements Base64EncodableImage {

    private static final int COMPRESSION_QUALITY = 70;
    private static final String TAG = FritzVisionImage.class.getSimpleName();

    private static BitmapFactory.Options opts = new BitmapFactory.Options();

    private enum Type {
        BITMAP,
        MEDIA_IMAGE,
        BYTE
    }

    /**
     * Convert a bitmap to a FritzVisionImage.
     *
     * @param bitmap The bitmap to convert.
     * @return A FritzVisionImage object.
     */
    public static FritzVisionImage fromBitmap(Bitmap bitmap) {
        return fromBitmap(bitmap, ImageOrientation.UP);
    }


    public static FritzVisionImage fromBitmap(Bitmap bitmap, ImageOrientation orientation) {
        return new FritzVisionImage(bitmap, orientation);
    }

    /**
     * Convert a YUVImage in its raw form to a FritzVisionImage.
     *
     * @param yuvImage The image to convert.
     * @return A FritzVisionImage object.
     */
    public static FritzVisionImage fromYUVImage(YUVImage yuvImage) {
        return fromYUVImage(yuvImage, ImageOrientation.UP);
    }

    public static FritzVisionImage fromYUVImage(YUVImage yuvImage, ImageOrientation orientation) {
        return new FritzVisionImage(yuvImage, orientation);
    }

    /**
     * Convert from a media image to a bitmap.
     * <p>
     * TODO: Need to test this out with other formats other than YUV_420.
     * https://developer.android.com/reference/android/media/Image
     *
     * @param image       The image to convert.
     * @param orientation the image orientation.
     * @return A FritzVisionImage object.
     */
    public static FritzVisionImage fromMediaImage(Image image, ImageOrientation orientation) {
        if (image.getFormat() == ImageFormat.YUV_420_888) {
            return new FritzVisionImage(image, orientation);
        }

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        return new FritzVisionImage(bitmapImage, orientation);
    }

    /**
     * Convert a byte representation of an image to a FritzVisionImage.
     *
     * @param byteImage The byte representation to convert.
     * @return A FritzVisionImage object.
     */
    public static FritzVisionImage fromByteImage(ByteImage byteImage) {
        return fromByteImage(byteImage, ImageOrientation.UP);
    }

    public static FritzVisionImage fromByteImage(ByteImage byteImage, ImageOrientation orientation) {
        return new FritzVisionImage(byteImage, orientation);
    }

    /**
     * Create a FritzVisionImage with FritzVisionImageFilter objects applied to it.
     *
     * @param source  The image to apply filters on.
     * @param filters The filters to apply.
     * @return A FritzVisionImage object.
     */
    public static FritzVisionImage applyingFilters(ByteImage source, FritzVisionImageFilter[] filters) {
        FritzVisionImage baseImage = FritzVisionImage.fromByteImage(source);
        FritzVisionImage resultImage = baseImage;

        for (FritzVisionImageFilter filter : filters) {
            switch (filter.getCompositionMode()) {
                case COMPOUND_WITH_PREVIOUS_OUTPUT:
                    resultImage = filter.processImage(resultImage);
                    break;
                case OVERLAY_ON_ORIGINAL_IMAGE:
                    Bitmap processed = filter.processImage(baseImage).buildOrientedBitmap();
                    Bitmap overlayImage = resultImage.overlay(processed);
                    resultImage = FritzVisionImage.fromBitmap(overlayImage);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid composition mode for FritzVisionImageFilter.");
            }
        }

        return resultImage;
    }

    /**
     * FritzVisionImage instance.
     */

    private Bitmap bitmap;
    private YUVImage yuvImage;
    private ByteImage byteImage;
    private Type type;

    private Allocation rotatedAllocation;
    private int rotatedWidth;
    private int rotatedHeight;
    private ImageOrientation orientation;

    private FritzVisionImage(Bitmap bitmap, ImageOrientation orientation) {
        this.bitmap = bitmap;
        this.type = Type.BITMAP;
        this.orientation = orientation;
    }

    private FritzVisionImage(Image image, ImageOrientation orientation) {
        this.yuvImage = new YUVImage(image);
        this.type = Type.MEDIA_IMAGE;
        this.orientation = orientation;
    }

    private FritzVisionImage(YUVImage yuvImage, ImageOrientation orientation) {
        this.yuvImage = yuvImage;
        this.type = Type.MEDIA_IMAGE;
        this.orientation = orientation;
    }

    private FritzVisionImage(ByteImage byteImage, ImageOrientation orientation) {
        this.byteImage = byteImage;
        this.type = Type.BYTE;
        this.orientation = orientation;
    }

    public void release() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }

        if (yuvImage != null) {
            yuvImage = null;
        }

        if (byteImage != null) {
            byteImage = null;
        }
    }

    public ImageOrientation getOrientation() {
        return orientation;
    }


    /**
     * Resizes the image and applies any specified rotations.
     *
     * @param modelInputSize The dimensions to resize the image to.
     * @return The prepared image as a byte representation.
     */
    public synchronized ByteImage prepareBytes(Size modelInputSize) {
        long start = System.currentTimeMillis();

        ImageProcessingPipeline pipeline = getOrientedImagePipeline();

        // Resize to model output
        if (modelInputSize != null) {
            pipeline.resize(modelInputSize);
        }

        ByteImage preparedImage = pipeline.buildByteImage();
        Log.d(TAG, "Image Processing Time(ms): " + (System.currentTimeMillis() - start));
        return preparedImage;
    }

    /**
     * Prepare the image by orientation only (no resize).
     *
     * @return a byte image
     */
    public synchronized ByteImage prepareBytes() {
        return prepareBytes(null);
    }

    /**
     * Checks to see if the image has been oriented.
     * <p>
     * This occurs when the image is passed into a predictor or is converted into a bitmap.
     *
     * @return true/false
     */
    private boolean hasRotatedAllocation() {
        return rotatedAllocation != null;
    }

    private ImageProcessingPipeline buildPipelineFromSource() {
        switch (type) {
            case BITMAP:
                return new ImageProcessingPipeline(bitmap);
            case MEDIA_IMAGE:
                return new ImageProcessingPipeline(yuvImage);
            default:
                return new ImageProcessingPipeline(byteImage);
        }
    }

    /**
     * Create a bitmap for the original image after the orientation is applied.
     *
     * @return a bitmap
     */
    public Bitmap buildOrientedBitmap() {
        ImageProcessingPipeline pipeline = getOrientedImagePipeline();
        return pipeline.buildBitmap();
    }

    /**
     * Create a byte image for the original image after the orientation is applied.
     *
     * @return a {@link ByteImage}
     */
    public ByteImage buildOrientedByteImage() {
        ImageProcessingPipeline pipeline = getOrientedImagePipeline();
        return pipeline.buildByteImage();
    }

    /**
     * Create a byte image for the original yuv image after the orientation is applied.
     *
     * Used in FritzVisionVideo
     *
     * @return a {@link ByteImage}
     */
    public ByteImage buildOrientedYuvByteImage() {
        ImageProcessingPipeline pipeline = getOrientedImagePipeline();
        return pipeline.buildYuvByteImage();
    }

    private ImageProcessingPipeline getOrientedImagePipeline() {
        // Prevent orienting the image twice.
        if (hasRotatedAllocation()) {
            return new ImageProcessingPipeline(rotatedAllocation, rotatedWidth, rotatedHeight);
        }

        // Orient the image and save the state after.
        ImageProcessingPipeline pipeline = buildPipelineFromSource();

        // Apply orientation
        pipeline.orient(orientation);
        rotatedAllocation = pipeline.getAllocation();
        rotatedWidth = pipeline.getWidth();
        rotatedHeight = pipeline.getHeight();

        return pipeline;
    }

    public int getRotatedWidth() {
        return rotatedWidth;
    }

    public int getRotatedHeight() {
        return rotatedHeight;
    }

    /**
     * Retrieve the width of the image.
     *
     * @return The width of the image.
     */
    public int getWidth() {
        switch (type) {
            case BITMAP:
                return bitmap.getWidth();
            case MEDIA_IMAGE:
                return yuvImage.getWidth();
            case BYTE:
                return byteImage.getWidth();
            default:
                throw new IllegalArgumentException("Invalid image type.");
        }
    }

    /**
     * Retrieve the height of the image.
     *
     * @return The height of the image.
     */
    public int getHeight() {
        switch (type) {
            case BITMAP:
                return bitmap.getHeight();
            case MEDIA_IMAGE:
                return yuvImage.getHeight();
            case BYTE:
                return byteImage.getHeight();
            default:
                throw new IllegalArgumentException("Invalid image type.");
        }
    }

    /**
     * Get the size of the image.
     *
     * @return image size.
     */
    public Size getSize() {
        return new Size(getWidth(), getHeight());
    }

    /**
     * Overlay a bitmap onto the original image.
     *
     * @param image - the image to superimpose on the original.
     * @return the original image with the mask overlay.
     */
    public Bitmap overlay(Bitmap image) {
        Bitmap sourceBitmap = buildOrientedBitmap();
        Bitmap output = sourceBitmap.copy(sourceBitmap.getConfig(), true);
        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(image, null, new RectF(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), null);

        return output;
    }

    /**
     * Overlay the Object Detection result onto the original image.
     *
     * @param visionObject - the object to draw.
     * @return a bitmap with the object on the original image.
     */
    public Bitmap overlayBoundingBox(FritzVisionObject visionObject) {
        Bitmap sourceBitmap = buildOrientedBitmap();
        Bitmap output = sourceBitmap.copy(sourceBitmap.getConfig(), true);
        Canvas canvas = new Canvas(output);
        visionObject.draw(canvas);

        return output;
    }

    /**
     * Overlay the Object Detection results onto the original image.
     *
     * @param visionObjects - the objects to draw.
     * @return a bitmap with the objects on the original image.
     */
    public Bitmap overlayBoundingBoxes(List<FritzVisionObject> visionObjects) {
        Bitmap sourceBitmap = buildOrientedBitmap();
        Bitmap output = sourceBitmap.copy(sourceBitmap.getConfig(), true);
        Canvas canvas = new Canvas(output);

        for (FritzVisionObject visionObject : visionObjects) {
            visionObject.draw(canvas);
        }

        return output;
    }

    /**
     * Overlay the Pose estimation results onto the original image.
     *
     * @param pose - the pose to draw.
     * @return a bitmap with the pose on the original image.
     */
    public Bitmap overlaySkeleton(Pose pose) {
        Bitmap sourceBitmap = buildOrientedBitmap();
        Bitmap output = sourceBitmap.copy(sourceBitmap.getConfig(), true);
        Canvas canvas = new Canvas(output);
        pose.draw(canvas);

        return output;
    }

    /**
     * Overlay the Pose estimation results onto the original image.
     *
     * @param poses - the poses to draw.
     * @return a bitmap with the poses on the original image.
     */
    public Bitmap overlaySkeletons(List<Pose> poses) {
        Bitmap sourceBitmap = buildOrientedBitmap();
        Bitmap output = sourceBitmap.copy(sourceBitmap.getConfig(), true);
        Canvas canvas = new Canvas(output);

        for (Pose pose : poses) {
            pose.draw(canvas);
        }

        return output;
    }

    /**
     * Crop the masked section from the image.
     * <p>
     * The output will have the same dimensions as the original image.
     *
     * @return a bitmap of the cropped section.
     */
    public Bitmap mask(Bitmap mask) {
        return mask(mask, false);
    }

    /**
     * Crop the masked section from the image.
     * <p>
     * Pass in an alpha mask of the section you'd like th crop from the original image.
     * The output will have the same dimensions as the original image if trim is false.
     *
     * @param mask - the alpha mask to crop from the image.
     * @param trim - trim the extra transparent pixels from the result.
     * @return a bitmap of the cropped section.
     */
    public Bitmap mask(Bitmap mask, boolean trim) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        Bitmap sourceBitmap = buildOrientedBitmap();

        Bitmap output = Bitmap.createBitmap(sourceBitmap.getWidth(),
                sourceBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(mask, null, new RectF(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), null);
        canvas.drawBitmap(sourceBitmap, 0, 0, paint);

        if (trim) {
            return trimBounds(output);
        }

        return output;
    }

    /**
     * Trim the bounds of an image.
     *
     * @param source The image to trim.
     * @return The trimmed image.
     */
    private Bitmap trimBounds(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);

        int leftMin = width;
        int rightMax = 0;
        int topMax = 0;
        int bottomMin = height;

        boolean detected = false;

        for (int row = 0; row < source.getHeight(); row++) {
            for (int col = 0; col < source.getWidth(); col++) {
                int color = pixels[row * source.getWidth() + col];
                if (color == Color.TRANSPARENT) {
                    continue;
                }
                leftMin = Math.min(col, leftMin);
                rightMax = Math.max(col, rightMax);
                topMax = Math.max(row, topMax);
                bottomMin = Math.min(row, bottomMin);
                detected = true;
            }
        }

        // If the class was not detected, don't create the source.
        if (!detected) {
            return null;
        }

        RectF sourceBounds = new RectF(
                leftMin,
                topMax,
                rightMax,
                bottomMin
        );

        int boundWidth = rightMax - leftMin;
        int boundHeight = topMax - bottomMin;

        return Bitmap.createBitmap(source, (int) sourceBounds.left, (int) sourceBounds.bottom, boundWidth, boundHeight);
    }

    /**
     * Blend a bitmap on top of the original image.
     *
     * @param mask      The mask to blend with the original image.
     * @param blendMode The type of blend.
     * @return The original image blended with a mask.
     */
    public Bitmap blend(Bitmap mask, BlendMode blendMode) {
        long start = System.currentTimeMillis();

        Bitmap bitmap = buildOrientedBitmap();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap resizedMaskBitmap = Bitmap.createScaledBitmap(mask, width, height, false);

        GPUImage gpuImage = new GPUImage(Fritz.getAppContext());
        GPUImageTwoInputFilter blendFilter = getGPUFilter(blendMode);
        blendFilter.setBitmap(resizedMaskBitmap);
        gpuImage.setImage(bitmap);
        gpuImage.setFilter(blendFilter);

        Bitmap output = gpuImage.getBitmapWithFilterApplied();
        Log.d(TAG, "Blend Time: " + (System.currentTimeMillis() - start));
        return output;
    }

    private GPUImageTwoInputFilter getGPUFilter(BlendMode blendMode) {
        switch (blendMode) {
            case HUE:
                return new GPUImageHueBlendFilter();
            case COLOR:
                return new GPUImageColorBlendFilter();
            default:
                return new GPUImageSoftLightBlendFilter();
        }
    }

    @NotNull
    @Override
    public String encodedInput() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap sourceBitmap = buildOrientedBitmap();
        sourceBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
    }

    @NotNull
    @Override
    public Size encodedSize() {
        if (rotatedAllocation != null) {
            return new Size(rotatedWidth, rotatedHeight);
        }

        return getSize();
    }

    @NotNull
    @Override
    public String encodedImageFormat() {
        return "jpeg";
    }
}