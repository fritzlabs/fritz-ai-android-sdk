package ai.fritz.vision.video.filters.imagesegmentation;

import android.graphics.Bitmap;

import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationMaskOptions;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictor;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationResult;
import ai.fritz.vision.imagesegmentation.MaskClass;
import ai.fritz.core.FritzOnDeviceModel;

/**
 * Filter for creating a mask over a type of object.
 */
public class MaskOverlayFilter extends FritzVisionSegmentationFilter {

    public MaskOverlayFilter(
            FritzVisionSegmentationPredictor predictor,
            MaskClass segmentationMask
    ) {
        super(predictor, segmentationMask);
    }

    public MaskOverlayFilter(
            FritzVisionSegmentationPredictor predictor,
            FritzVisionSegmentationMaskOptions options,
            MaskClass segmentationMask
    ) {
        super(predictor, options, segmentationMask);
    }

    @Override
    public FilterCompositionMode getCompositionMode() {
        return FilterCompositionMode.OVERLAY_ON_ORIGINAL_IMAGE;
    }

    @Override
    public FritzVisionImage processImage(FritzVisionImage image) {
        FritzVisionSegmentationResult hairResult = predictor.predict(image);
        Bitmap mask = hairResult.buildSingleClassMask(segmentationMask, options);
        return FritzVisionImage.fromBitmap(mask);
    }
}
