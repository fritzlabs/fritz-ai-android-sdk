package ai.fritz.sdktests.poseestimation;

import android.util.Pair;

import ai.fritz.vision.poseestimation.Skeleton;


public class CustomSkeletonWithoutPoseChain extends Skeleton {
    public static String[] KEYPOINT_NAMES = {
            "thumb", "index", "middle", "ring", "pinky"
    };

    public CustomSkeletonWithoutPoseChain() {
        super("hand", KEYPOINT_NAMES);
    }
}