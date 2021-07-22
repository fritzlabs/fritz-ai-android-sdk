package ai.fritz.vision.filter;

import ai.fritz.vision.poseestimation.Pose;

public interface PoseSmoother {

    Pose smooth(Pose pose);
}
