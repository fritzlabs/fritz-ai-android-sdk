package ai.fritz.vision.video.filters.imagesegmentation;

import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationMaskOptions;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictor;
import ai.fritz.vision.imagesegmentation.MaskClass;
import ai.fritz.vision.video.FritzVisionImageFilter;

public abstract class FritzVisionSegmentationFilter extends FritzVisionImageFilter {

    protected FritzVisionSegmentationPredictor predictor;
    protected FritzVisionSegmentationMaskOptions options;
    protected MaskClass segmentationMask;

    public FritzVisionSegmentationFilter(
            FritzVisionSegmentationPredictor model,
            MaskClass segmentationMask
    ) {
        this(model, new FritzVisionSegmentationMaskOptions(), segmentationMask);
    }

    public FritzVisionSegmentationFilter(
            FritzVisionSegmentationPredictor predictor,
            FritzVisionSegmentationMaskOptions options,
            MaskClass segmentationMask
    ) {
        this.predictor = predictor;
        this.options = options;
        this.segmentationMask = segmentationMask;
    }
}
