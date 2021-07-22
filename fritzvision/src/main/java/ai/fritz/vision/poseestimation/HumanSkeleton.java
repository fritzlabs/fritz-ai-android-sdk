package ai.fritz.vision.poseestimation;

import android.util.Pair;

public class HumanSkeleton extends Skeleton {

    public static String[] PART_NAMES = {
            "nose", "leftEye", "rightEye", "leftEar", "rightEar", "leftShoulder",
            "rightShoulder", "leftElbow", "rightElbow", "leftWrist", "rightWrist",
            "leftHip", "rightHip", "leftKnee", "rightKnee", "leftAnkle", "rightAnkle"
    };

    public static Pair[] CONNECTED_PART_NAMES = {
            new Pair<>("leftHip", "leftShoulder"),
            new Pair<>("leftElbow", "leftShoulder"),
            new Pair<>("leftElbow", "leftWrist"),
            new Pair<>("leftHip", "leftKnee"),
            new Pair<>("leftKnee", "leftAnkle"),
            new Pair<>("rightHip", "rightShoulder"),
            new Pair<>("rightElbow", "rightShoulder"),
            new Pair<>("rightElbow", "rightWrist"),
            new Pair<>("rightHip", "rightKnee"),
            new Pair<>("rightKnee", "rightAnkle"),
            new Pair<>("leftShoulder", "rightShoulder"),
            new Pair<>("leftHip", "rightHip")
    };

        public static Pair[] POSE_CHAIN = {
            new Pair<>("nose", "leftEye"),
            new Pair<>("leftEye", "leftEar"),
            new Pair<>("nose", "rightEye"),
            new Pair<>("rightEye", "rightEar"),
            new Pair<>("nose", "leftShoulder"),
            new Pair<>("leftShoulder", "leftElbow"),
            new Pair<>("leftElbow", "leftWrist"),
            new Pair<>("leftShoulder", "leftHip"),
            new Pair<>("leftHip", "leftKnee"),
            new Pair<>("leftKnee", "leftAnkle"),
            new Pair<>("nose", "rightShoulder"),
            new Pair<>("rightShoulder", "rightElbow"),
            new Pair<>("rightElbow", "rightWrist"),
            new Pair<>("rightShoulder", "rightHip"),
            new Pair<>("rightHip", "rightKnee"),
            new Pair<>("rightKnee", "rightAnkle")
    };

    public HumanSkeleton() {
        super("Human", PART_NAMES, CONNECTED_PART_NAMES, POSE_CHAIN);
    }
}
