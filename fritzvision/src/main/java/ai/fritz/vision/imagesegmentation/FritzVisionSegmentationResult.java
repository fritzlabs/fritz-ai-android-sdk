package ai.fritz.vision.imagesegmentation;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Size;

import java.util.ArrayList;
import java.util.List;

import ai.fritz.core.annotations.DataAnnotation;
import ai.fritz.core.annotations.KeypointAnnotation;
import ai.fritz.core.annotations.SegmentationAnnotation;

/**
 * FritzVisionSegmentationResult holds the result from the {@link FritzVisionSegmentationPredictor#predict} method.
 */
public class FritzVisionSegmentationResult {
    private static final int MASK_RGB = 0x00FFFFFF;

    private static final int DEFAULT_ALPHA_VALUE = 255;
    private static final int ALPHA_SHIFT = 24;
    private static final float DEFAULT_BLUR_RADIUS = 0f;

    private int[][] classifications;
    private float[][] confidence;
    private MaskClass[] maskClasses;
    private Size targetInferenceSize;
    private Size modelOutputSize;
    private int offsetX;
    private int offsetY;

    private float confidenceThreshold;

    public FritzVisionSegmentationResult(FritzVisionSegmentationPredictorOptions options, MaskClass[] maskClasses, Size targetInferenceSize, Size modelOutputSize, int offsetX, int offsetY, int[][] classifications, float[][] confidence) {
        this.classifications = classifications;
        this.confidence = confidence;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.targetInferenceSize = targetInferenceSize;
        this.modelOutputSize = modelOutputSize;
        this.maskClasses = maskClasses;
        this.confidenceThreshold = options.confidenceThreshold;
    }

    /**
     * Get the confidence scores for each classification.
     * <p>
     * The dimensions on the output matrix match the model output size.
     *
     * @return a matrix[row][col] of float scores.
     */
    public float[][] getConfidenceScores() {
        return confidence;
    }

    /**
     * Get the raw mask classifications for each point on the model output.
     * <p>
     * The dimensions on the output matrix match the model output size.
     *
     * @return a matrix [row][col] of MaskTypes
     */
    public MaskClass[][] getMaskClassifications() {
        MaskClass[][] maskClassifications = new MaskClass[modelOutputSize.getHeight()][modelOutputSize.getWidth()];
        for (int row = 0; row < modelOutputSize.getHeight(); row++) {
            for (int col = 0; col < modelOutputSize.getWidth(); col++) {
                int maskIndex = classifications[row][col];
                maskClassifications[row][col] = maskClasses[maskIndex];
            }
        }
        return maskClassifications;
    }

    /**
     * Create a mask of the overlay to apply to an image.
     * This will have the model output dimensions.
     * <p>
     * Default alpha value is 60.
     * Default blur radius is 0.
     *
     * @return a bitmap of the overlay
     */
    public Bitmap buildMultiClassMask() {
        return buildMultiClassMask(DEFAULT_ALPHA_VALUE, 1, confidenceThreshold);
    }

    public Bitmap buildMultiClassMask(int maxAlpha, float clippingScoresAbove, float zeroingScoresBelow) {
        return buildMultiClassMask(maxAlpha, clippingScoresAbove, zeroingScoresBelow, DEFAULT_BLUR_RADIUS);
    }

    /**
     * Create a mask of the overlay to apply to an image.
     * This will have the model output dimensions.
     *
     * @param maxAlpha            - value between 0-255 for the overlay.
     * @param clippingScoresAbove - scores above this threshold will set the alpha value as 1
     * @param zeroingScoresBelow  - scores below this threshold will set the alpha value as 0
     * @param blurRadius          - extent to blur the edges of the mask between 1-25
     * @return a bitmap of the overlay
     */
    public Bitmap buildMultiClassMask(int maxAlpha, float clippingScoresAbove, float zeroingScoresBelow, float blurRadius) {
        // Set the alpha colors for quick indexing
        int[] maskIndexToColor = new int[maskClasses.length];
        for (int i = 0; i < maskClasses.length; i++) {
            int color = maskClasses[i].getColorIdentifier();
            maskIndexToColor[i] = color;
        }

        int[] colors = getColorsFromMask(maskIndexToColor, maxAlpha, clippingScoresAbove, zeroingScoresBelow);
        Bitmap bitmap = Bitmap.createBitmap(colors, modelOutputSize.getWidth(), modelOutputSize.getHeight(), Bitmap.Config.ARGB_8888);
        if (blurRadius > DEFAULT_BLUR_RADIUS) {
            MaskPostProcessOptions maskOptions = new MaskPostProcessOptions(bitmap);
            maskOptions.addBlur(blurRadius);
        }

        return bitmap;
    }

    /**
     * Create a bitmap for the masked section of the image.
     * <p>
     * Default alpha value is 60.
     * Default blur radius is 0.
     *
     * @param maskClass - the mask to create a bitmap of.
     * @return an Optional Bitmap for the masked pixels. Will return null if the class was not detected.
     */
    public Bitmap buildSingleClassMask(MaskClass maskClass) {
        return buildSingleClassMask(maskClass, DEFAULT_ALPHA_VALUE, 1, confidenceThreshold);
    }

    public Bitmap buildSingleClassMask(MaskClass maskClass, int maxAlpha, float clippingScoresAbove, float zeroingScoresBelow) {
        // Special case where the mask to find is none.
        int color = (maskClass == MaskClass.NONE) ? Color.BLACK : maskClass.getColorIdentifier();
        return buildSingleClassMask(maskClass, maxAlpha, clippingScoresAbove, zeroingScoresBelow, color);
    }

    public Bitmap buildSingleClassMask(MaskClass maskClass, int maxAlpha, float clippingScoresAbove, float zeroingScoresBelow, int maskColor) {
        return buildSingleClassMask(maskClass, maxAlpha, clippingScoresAbove, zeroingScoresBelow, maskColor, DEFAULT_BLUR_RADIUS);
    }

    /**
     * Create a bitmap for the masked section of the image.
     * <p>
     * Cut out the masked layer from the original image and create a new bitmap.
     * If no pixels are associated with the mask type, return null.
     * <p>
     * Example:
     * <pre>{@code
     *  Bitmap personBitmap = result.buildSingleClassMask(MaskClass.PERSON, .8f, .5f);
     *  if(person != null) {
     *      canvas.drawBitmap(personBitmap, ...);
     *  }
     * }</pre>
     *
     * @param maskClass           - the mask type used in the alpha mask.
     * @param maxAlpha            - the maximum alpha value.
     * @param clippingScoresAbove - scores above this threshold will set the alpha value as the max alpha value
     * @param zeroingScoresBelow  - scores below this threshold will set the alpha value as 0
     * @param maskColor           - the color to set the mask
     * @param blurRadius          - extent to blur the edges of the mask between 1-25
     * @return an Optional Bitmap for the masked pixels. Will return null if the class was not detected.
     */
    public Bitmap buildSingleClassMask(MaskClass maskClass, int maxAlpha, float clippingScoresAbove, float zeroingScoresBelow, int maskColor, float blurRadius) {
        // Set the alpha colors for quick indexing
        int[] maskIndexToColor = new int[maskClasses.length];
        for (int i = 0; i < maskClasses.length; i++) {
            MaskClass maskClassToCompare = maskClasses[i];

            if (maskClass.label.equalsIgnoreCase(maskClassToCompare.label)) {
                maskIndexToColor[i] = maskColor;
            } else {
                maskIndexToColor[i] = 0;
            }
        }

        int[] colors = getColorsFromMask(maskIndexToColor, maxAlpha, clippingScoresAbove, zeroingScoresBelow);
        Bitmap bitmap = Bitmap.createBitmap(colors, modelOutputSize.getWidth(), modelOutputSize.getHeight(), Bitmap.Config.ARGB_8888);
        if (blurRadius > DEFAULT_BLUR_RADIUS) {
            MaskPostProcessOptions maskOptions = new MaskPostProcessOptions(bitmap);
            maskOptions.addBlur(blurRadius);
        }

        return bitmap;
    }

    public Bitmap buildSingleClassMask(MaskClass maskClass, FritzVisionSegmentationMaskOptions options) {
        int maskColor = options.maskColor == Color.TRANSPARENT ? maskClass.color : options.maskColor;
        return buildSingleClassMask(maskClass, options.maxAlpha, options.clippingScoresAbove, options.confidenceThreshold, maskColor, options.blurRadius);
    }

    private int[] getColorsFromMask(int[] maskIndexToColor, int maxAlpha, float clippingScoresAbove, float zeroingScoresBelow) {
        int outputWidth = modelOutputSize.getWidth();
        int outputHeight = modelOutputSize.getHeight();

        int[] colors = new int[outputHeight * outputWidth];

        // Create an array of colors
        for (int row = 0; row < outputHeight; row++) {
            for (int col = 0; col < outputWidth; col++) {
                int maskTypeIndex = classifications[row][col];
                float pointConfidence = confidence[row][col];

                // If the color is transparent, set it and be done.
                if (maskIndexToColor[maskTypeIndex] == 0) {
                    colors[row * outputWidth + col] = 0;
                    continue;
                }

                if (pointConfidence > clippingScoresAbove) {
                    int color = (maxAlpha << ALPHA_SHIFT) | maskIndexToColor[maskTypeIndex] & MASK_RGB;
                    colors[row * outputWidth + col] = color;
                    continue;
                }

                if (pointConfidence < zeroingScoresBelow) {
                    colors[row * outputWidth + col] = 0;
                    continue;
                }

                float normalized = Math.max(0, (pointConfidence - zeroingScoresBelow) / (clippingScoresAbove - zeroingScoresBelow));
                int alphaScaled = (int) Math.min(normalized * maxAlpha, maxAlpha);
                int color = (alphaScaled << ALPHA_SHIFT) | maskIndexToColor[maskTypeIndex] & MASK_RGB;
                colors[row * outputWidth + col] = color;
            }
        }

        return colors;
    }


    /**
     * Create an annotation object for a specific mask class.
     *
     * @param maskClass - the class to create the annotation for
     * @param confidenceThreshold - pixels with confidence above this value will be included
     *                            in the annotation
     * @return a DataAnnotation object with the segmentation mask for this class.
     */
    public DataAnnotation toAnnotation(MaskClass maskClass, float confidenceThreshold) {
        int outputWidth = modelOutputSize.getWidth();
        int outputHeight = modelOutputSize.getHeight();

        // Start with -1 so that any classes not in maskClasses for the model will result in
        // arrays of 0s.
        int maskIndex = -1;
        for (int idx = 0; idx < maskClasses.length; idx++) {
            if (maskClasses[idx].label == maskClass.label) {
                maskIndex = idx;
            }
        }

        int[][] mask = new int[outputHeight][outputWidth];
        for (int row = 0; row < outputHeight; row++) {
            for (int col = 0; col < outputWidth; col++) {
                if (classifications[row][col] == maskIndex && confidence[row][col] >= confidenceThreshold) {
                    mask[row][col] = 1;
                }
            }
        }
        return new DataAnnotation(maskClass.label, new ArrayList<KeypointAnnotation>(), null, new SegmentationAnnotation(mask),  false);
    }


    /**
     * Create a list of annotations for predictions from the model.
     *
     * This method loops over all classes predicted by the model and constructs an annotation
     * if enough high confidence pixels are found for a given class.
     *
     * @param confidenceThreshold - pixels with confidence above this value will be included
     *                            in the annotation
     * @param areaThreshold - the fraction of pixels above the confidenceThreshold that must be
     *                      present for a class in order to create a DataAnnotation. This is used
     *                      to filter out small objects from annotations.
     * @return a list of DataAnnotations with the segmentation mask for classes meeting thresholds.
     */
    public List<DataAnnotation> toAnnotations(float confidenceThreshold, float areaThreshold) {
        int outputWidth = modelOutputSize.getWidth();
        int outputHeight = modelOutputSize.getHeight();

        List<DataAnnotation> annotations = new ArrayList<>();
        for (int i = 0; i < maskClasses.length; i++) {

            // Never send "none" classes back to the image collection.
            if (maskClasses[i].label.equals("None")) {
                continue;
            }

            DataAnnotation annotation = this.toAnnotation(maskClasses[i], confidenceThreshold);

            int[][] mask = annotation.getSegmentation().getMask();
            int total = 0;
            for (int row = 0; row < outputHeight; row++) {
                for (int col = 0; col < outputWidth; col++) {
                    total += mask[row][col];
                }
            }
            float area = (float) total / (float) outputHeight * outputWidth;
            if (area > areaThreshold) {
                annotations.add(annotation);
            }
        }
        return annotations;
    }

    public List<DataAnnotation> toAnnotations() {
        return this.toAnnotations(0.5f, 0.1f);
    }
}
