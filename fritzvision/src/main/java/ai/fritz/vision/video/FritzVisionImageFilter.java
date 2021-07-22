package ai.fritz.vision.video;

import ai.fritz.vision.FritzVisionImage;

public abstract class FritzVisionImageFilter {

    public enum FilterCompositionMode {

        /**
         * Use the previous image as a predictor.
         * The output image will be the result of the filter.
         */
        COMPOUND_WITH_PREVIOUS_OUTPUT,

        /**
         * Use the original image as a predictor.
         * The output image will be the result of the filter overlaid on top of the input image.
         */
        OVERLAY_ON_ORIGINAL_IMAGE
    }

    /**
     * Retrieves the composition mode for this filter.
     *
     * @return The composition mode.
     */
    public abstract FilterCompositionMode getCompositionMode();

    /**
     * Processes the image with a prediction.
     *
     * @param image The image to process.
     * @return The result of the prediction.
     */
    public abstract FritzVisionImage processImage(FritzVisionImage image);
}
