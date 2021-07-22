package ai.fritz.sdktests.poseestimation;

import android.util.Pair;

import ai.fritz.vision.poseestimation.Skeleton;

// Create a file called CustomSkeleton.java
public class CustomSkeleton extends Skeleton {
    public static String[] KEYPOINT_NAMES = {
            "thumb", "index", "middle", "ring", "pinky"
    };

    public static Pair[] CONNECTED_PART_NAMES = {
            new Pair<>("thumb", "index"),
            new Pair<>("index", "middle"),
            new Pair<>("middle", "ring"),
            new Pair<>("ring", "pinky")
    };

    public static Pair[] POSE_CHAIN = {
            new Pair<>("thumb", "index"),
            new Pair<>("index", "middle"),
            new Pair<>("middle", "ring"),
            new Pair<>("ring", "pinky")
    };

    public CustomSkeleton() {
        super("hand", KEYPOINT_NAMES, CONNECTED_PART_NAMES, POSE_CHAIN);
    }
}