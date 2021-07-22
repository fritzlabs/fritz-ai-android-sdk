package ai.fritz.vision.video.filters.imagesegmentation;

import android.graphics.Bitmap;

import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.imagesegmentation.BlendMode;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationMaskOptions;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictor;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationResult;
import ai.fritz.vision.imagesegmentation.MaskClass;

public class MaskBlendCompoundFilter extends FritzVisionSegmentationFilter {

    protected BlendMode blendMode;

    public MaskBlendCompoundFilter(
            FritzVisionSegmentationPredictor predictor,
            MaskClass segmentationMask,
            BlendMode blendMode
    ) {
        super(predictor, segmentationMask);
        this.blendMode = blendMode;
    }

    public MaskBlendCompoundFilter(
            FritzVisionSegmentationPredictor predictor,
            FritzVisionSegmentationMaskOptions options,
            MaskClass segmentationMask,
            BlendMode blendMode
    ) {
        super(predictor, options, segmentationMask);
        this.blendMode = blendMode;
    }

    @Override
    public FilterCompositionMode getCompositionMode() {
        return FilterCompositionMode.COMPOUND_WITH_PREVIOUS_OUTPUT;
    }

    @Override
    public FritzVisionImage processImage(FritzVisionImage image) {
        FritzVisionSegmentationResult hairResult = predictor.predict(image);
        Bitmap mask = hairResult.buildSingleClassMask(segmentationMask, options);
        return FritzVisionImage.fromBitmap(image.blend(mask, blendMode));
    }
}
