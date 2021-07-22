package ai.fritz.vision.poseestimation;

import android.util.Size;

import java.util.ArrayList;
import java.util.List;

import ai.fritz.core.annotations.DataAnnotation;
import ai.fritz.core.annotations.AnnotatableResult;

/**
 * A developer-friendly class that contains the post-processed result from the PoseEstimation model.
 */
public class FritzVisionPoseResult implements AnnotatableResult {
    private static final String TAG = FritzVisionPoseResult.class.getSimpleName();

    private List<Pose> poses;
    private float minPoseConfidence;
    private Size inputSize;
    private Size sourceInputSize;

    public FritzVisionPoseResult(List<Pose> poses, float minPoseConfidence, Size inputSize, Size sourceInputSize) {
        this.poses = poses;
        this.minPoseConfidence = minPoseConfidence;
        this.inputSize = inputSize;
        this.sourceInputSize = sourceInputSize;
    }

    /**
     * Get a list of poses returned from the model.
     *
     * @return a list of poses.
     */
    public List<Pose> getPoses() {
        return getPosesByThreshold(minPoseConfidence);
    }

    public List<Pose> getPosesByThreshold(float minConfidence) {
        List<Pose> resultPoses = new ArrayList<>();
        for (Pose pose : poses) {
            if (pose.getScore() >= minConfidence) {
                resultPoses.add(pose);
            }
        }

        return resultPoses;
    }

    @Override
    public List<DataAnnotation> toAnnotations() {
        List<DataAnnotation> annotations = new ArrayList<>();
        for (Pose pose : poses) {
            annotations.add(pose.toAnnotation(sourceInputSize));
        }
        return annotations;
    }
}
