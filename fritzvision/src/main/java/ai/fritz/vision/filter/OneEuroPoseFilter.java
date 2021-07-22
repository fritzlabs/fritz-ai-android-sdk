package ai.fritz.vision.filter;

import android.graphics.PointF;

import ai.fritz.vision.poseestimation.Keypoint;
import ai.fritz.vision.poseestimation.Pose;

public class OneEuroPoseFilter {

    private OneEuroPointFilter[] keypointFilters;

    public OneEuroPoseFilter(int numKeypoints, double minCutoff, double beta, double derivateCutoff) {
        keypointFilters = new OneEuroPointFilter[numKeypoints];

        for (int i = 0; i < keypointFilters.length; i++) {
            keypointFilters[i] = new OneEuroPointFilter(
                    minCutoff,
                    beta,
                    derivateCutoff
            );
        }
    }

    public PointF[] filter(Pose pose) {

        Keypoint[] keypoints = pose.getKeypoints();
        PointF[] points = new PointF[keypoints.length];
        for (int i = 0; i < keypoints.length; i++) {
            Keypoint keypoint = keypoints[i];
            points[i] = keypointFilters[i].filter(keypoint.getPosition());
        }

        return points;
    }
}
