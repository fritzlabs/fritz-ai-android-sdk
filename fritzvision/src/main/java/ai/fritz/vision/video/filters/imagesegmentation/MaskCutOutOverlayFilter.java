package ai.fritz.vision.video.filters.imagesegmentation;

import android.graphics.Bitmap;

import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationMaskOptions;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictor;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationResult;
import ai.fritz.vision.imagesegmentation.MaskClass;

/**
 * Filter for cutting out a type of object from the background.
 */
public class MaskCutOutOverlayFilter extends FritzVisionSegmentationFilter {

    public MaskCutOutOverlayFilter(
            FritzVisionSegmentationPredictor predictor,
            MaskClass segmentationMask
    ) {
        super(predictor, segmentationMask);
    }

    public MaskCutOutOverlayFilter(
            FritzVisionSegmentationPredictor model,
            FritzVisionSegmentationMaskOptions options,
            MaskClass segmentationMask
    ) {
        super(model, options, segmentationMask);
    }

    @Override
    public FilterCompositionMode getCompositionMode() {
        return FilterCompositionMode.OVERLAY_ON_ORIGINAL_IMAGE;
    }

    @Override
    public FritzVisionImage processImage(FritzVisionImage image) {
        FritzVisionSegmentationResult peopleResult = predictor.predict(image);
        Bitmap mask = peopleResult.buildSingleClassMask(segmentationMask, options);
        return FritzVisionImage.fromBitmap(image.mask(mask));
    }
}
