package ai.fritz.visionCV.rigidpose;

import org.tensorflow.lite.Interpreter;

import ai.fritz.vision.base.FritzVisionPredictorOptions;

public class FritzVisionRigidPosePredictorOptions extends FritzVisionPredictorOptions {
    public float confidenceThreshold = .2f;
    public int numKeypointsAboveThreshold = 3;
}
