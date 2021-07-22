package ai.fritz.vision.imagesegmentation;

import java.util.List;

import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.base.FritzVisionPredictorOptions;

/**
 * A tuning class to specify which predictions should be returned
 * by {@link FritzVisionSegmentationPredictor#predict(FritzVisionImage)}
 */
public class FritzVisionSegmentationPredictorOptions extends FritzVisionPredictorOptions {

    public List<MaskClass> targetClasses;
    public float clippingScoresAbove;
    public float confidenceThreshold;

    public FritzVisionSegmentationPredictorOptions() {
        super();
        this.clippingScoresAbove = .7f;
        this.confidenceThreshold = .3f;
        this.targetClasses = null;
    }
}

