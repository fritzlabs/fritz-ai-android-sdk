package ai.fritz.vision.filter;

import android.graphics.PointF;

import ai.fritz.vision.poseestimation.Keypoint;
import ai.fritz.vision.poseestimation.Pose;

public class EuroPoseSmoother implements PoseSmoother {
    private OneEuroPointFilter[] keypointFilters;

    public EuroPoseSmoother(int numKeypoints, double minCutoff, double beta, double derivateCutoff) {
        keypointFilters = new OneEuroPointFilter[numKeypoints];

        for (int i = 0; i < keypointFilters.length; i++) {
            keypointFilters[i] = new OneEuroPointFilter(
                    minCutoff,
                    beta,
                    derivateCutoff
            );
        }
    }

    public Pose smooth(Pose pose) {
        Keypoint[] keypoints = pose.getKeypoints();
        for (int i = 0; i < keypoints.length; i++) {
            Keypoint keypoint = keypoints[i];
            PointF smoothedPoint = keypointFilters[i].filter(keypoint.getPosition());
            keypoint.setPosition(smoothedPoint);
        }

        return new Pose(pose.getSkeleton(), keypoints, pose.getScore(), pose.getKeypointThreshold(), pose.getBounds());
    }
}
