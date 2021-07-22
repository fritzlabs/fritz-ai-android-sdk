package ai.fritz.vision;

import ai.fritz.vision.base.FritzVisionPredictor;

/**
 * A callback for when the predictor has downloaded the model and is ready to use.
 */
public interface PredictorStatusListener<T extends FritzVisionPredictor> {

    void onPredictorReady(T predictor);
}
