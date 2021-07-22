package ai.fritz.vision.imagelabeling;

import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.base.FritzVisionPredictorOptions;

/**
 * Options that developers can tune in order to specify which predictions should be returned
 * by {{@link FritzVisionLabelPredictor#predict(FritzVisionImage)}}
 */
public class FritzVisionLabelPredictorOptions extends FritzVisionPredictorOptions {

    public float confidenceThreshold;

    public FritzVisionLabelPredictorOptions() {
        super();
        confidenceThreshold = .3f;
    }
}
