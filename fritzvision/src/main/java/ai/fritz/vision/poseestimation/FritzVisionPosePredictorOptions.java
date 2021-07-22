package ai.fritz.vision.poseestimation;

import ai.fritz.vision.base.FritzVisionPredictorOptions;
import ai.fritz.vision.filter.PoseSmoothingMethod;

/**
 * A tuning class to specify which predictions should be returned
 */
public class FritzVisionPosePredictorOptions extends FritzVisionPredictorOptions {

    public int maxPosesToDetect;
    public float minPartThreshold;
    public float minPoseThreshold;
    public int nmsRadius;
    public PoseSmoothingMethod smoothingOptions;

    public FritzVisionPosePredictorOptions() {
        super();
        maxPosesToDetect = 1;
        minPartThreshold = .5f;
        minPoseThreshold = .2f;
        nmsRadius = 20;
        smoothingOptions = null;
    }
}

