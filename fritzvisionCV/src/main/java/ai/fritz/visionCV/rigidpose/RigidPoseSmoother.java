package ai.fritz.visionCV.rigidpose;

import com.google.ar.core.Pose;

import org.opencv.core.Point;

import ai.fritz.visionCV.filter.OneEuroPointFilter;
import ai.fritz.visionCV.filter.OneEuroPoseFilter;

public class RigidPoseSmoother {
    // To smooth the
    private static final double DEFAULT_POINT_DERIVATIVE_CUTOFF = .3;
    private static final double DEFAULT_POINT_MIN_CUTOFF = .2;
    private static final double DEFAULT_POINT_BETA = .01;

    // To smooth the AR Pose object
    private static final double DEFAULT_POSE_DERIVATIVE_CUTOFF = .5;
    private static final double DEFAULT_POSE_MIN_CUTOFF = .5;
    private static final double DEFAULT_POSE_BETA = 1;

    private OneEuroPointFilter[] filters;
    private OneEuroPoseFilter poseFilter;

    public static class SmoothingOptions {
        public double minCutoff;
        public double beta;
        public double derivativeCutoff;

        public SmoothingOptions(double minCutoff, double beta, double derivativeCutoff) {
            this.minCutoff = minCutoff;
            this.beta = beta;
            this.derivativeCutoff = derivativeCutoff;
        }
    }

    public RigidPoseSmoother(int numKeypoints) {
        this(numKeypoints,
                new SmoothingOptions(DEFAULT_POINT_MIN_CUTOFF, DEFAULT_POINT_BETA, DEFAULT_POINT_DERIVATIVE_CUTOFF),
                new SmoothingOptions(DEFAULT_POSE_MIN_CUTOFF, DEFAULT_POSE_BETA, DEFAULT_POSE_DERIVATIVE_CUTOFF));
    }

    public RigidPoseSmoother(
            int numKeypoints,
            SmoothingOptions pointOptions,
            SmoothingOptions poseOptions) {
        filters = new OneEuroPointFilter[numKeypoints];

        for (int i = 0; i < filters.length; i++) {
            filters[i] = new OneEuroPointFilter(pointOptions.minCutoff, pointOptions.beta, pointOptions.derivativeCutoff);
        }

        poseFilter = new OneEuroPoseFilter(poseOptions.minCutoff, poseOptions.beta, poseOptions.derivativeCutoff);
    }

    public RigidPoseResult smooth2DKeypoints(RigidPoseResult dentastixResult) {
        Point[] keypoints = dentastixResult.getKeypoints();
        Point[] smoothedKeypoints = new Point[keypoints.length];
        for (int i = 0; i < keypoints.length; i++) {
            Point keypoint = keypoints[i];
            OneEuroPointFilter oneEuroPointFilter = filters[i];
            smoothedKeypoints[i] = oneEuroPointFilter.filter(keypoint);
        }

        return new RigidPoseResult(
                smoothedKeypoints,
                dentastixResult.getScores(),
                dentastixResult.getGridSize());
    }

    public Pose smoothPoseRotation(Pose dentastixPose) {
        return poseFilter.filterRotation(dentastixPose);
    }

    public Pose smoothPoseTranslation(Pose dentastixPose) {
        return poseFilter.filterTranslation(dentastixPose);
    }

    public Pose smoothPose(Pose dentastixPose) {
        return poseFilter.filter(dentastixPose);
    }
}
