package ai.fritz.vision.objectdetection;

import org.tensorflow.lite.Interpreter;

import java.util.List;

import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.base.FritzVisionPredictorOptions;
import ai.fritz.vision.base.LabelsManager;

/**
 * A tuning class to specify which predictions should be returned
 * by {{@link FritzVisionObjectPredictor#predict(FritzVisionImage)}}
 */
public class FritzVisionObjectPredictorOptions extends FritzVisionPredictorOptions {

    public float confidenceThreshold;
    public float iouThreshold;

    public FritzVisionObjectPredictorOptions() {
        super();
        confidenceThreshold = .6f;
        iouThreshold = .2f;
    }
}

