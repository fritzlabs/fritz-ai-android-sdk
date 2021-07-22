package ai.fritz.vision.poseestimation;

import android.graphics.Canvas;
import android.util.Pair;
import android.util.Size;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import ai.fritz.core.annotations.DataAnnotation;
import ai.fritz.core.annotations.AnnotatableObject;
import ai.fritz.core.annotations.KeypointAnnotation;
import ai.fritz.vision.base.DrawingUtils;

/**
 * A list of keypoints and an associated score.
 */
public class Pose implements AnnotatableObject {

    private Keypoint[] keypoints;
    private float poseScore;
    private float keypointThreshold;
    private Size bounds;
    private Skeleton skeleton;

    public Pose(Skeleton skeleton, Keypoint[] keypoints, float poseScore, float keypointThreshold, Size bounds) {
        this.keypoints = keypoints;
        this.poseScore = poseScore;
        this.keypointThreshold = keypointThreshold;
        this.bounds = bounds;
        this.skeleton = skeleton;
    }

    /**
     * Get all keypoints for this Pose.
     *
     * @return an array of keypoints.
     */
    public Keypoint[] getKeypoints() {
        return keypoints;
    }

    /**
     * Get the score of the Pose
     *
     * @return a float score from 0-1
     */
    public float getScore() {
        return poseScore;
    }

    /**
     * Get the keypoint threshold.
     *
     * @return the threshold.
     */
    public float getKeypointThreshold() {
        return keypointThreshold;
    }

    /**
     * Gets the bounds for the keypoint coordinates.
     *
     * @return the bounds.
     */
    public Size getBounds() {
        return bounds;
    }

    /**
     * Get the skeleton of the pose.
     *
     * @return the skeleton.
     */
    public Skeleton getSkeleton() {
        return skeleton;
    }

    /**
     * Create a new Pose with updated bounds. This will change the keypoint positions
     *
     * @param newBounds - the new bounds used to update the keypoint coordinates.
     * @return
     */
    public Pose scaledTo(Size newBounds) {
        return new Pose(skeleton, scaleKeypoints(keypoints, newBounds.getWidth(), newBounds.getHeight()), poseScore, keypointThreshold, bounds);
    }

    private Keypoint[] scaleKeypoints(Keypoint[] keypoints, int width, int height) {

        Keypoint[] updatedKeypoints = new Keypoint[keypoints.length];
        Size scaledSize = new Size(width, height);
        for (int i = 0; i < keypoints.length; i++) {
            Keypoint scaledKeypoints = keypoints[i].scaled(scaledSize);
            updatedKeypoints[i] = scaledKeypoints;
        }

        return updatedKeypoints;
    }

    /**
     * Get a list of the connected keypoints if the keypoint confidence scores are higher than a given threshold.
     *
     * @return a list of connected keypoints to draw a line between.
     */
    public List<Pair<Keypoint, Keypoint>> getConnectedKeypoints(Keypoint[] keypoints) {
        List<Pair<Keypoint, Keypoint>> connectedKeypoints = new ArrayList<>();
        for (Pair<Integer, Integer> connectedParts : skeleton.getConnectedKeypointIndicies()) {
            Keypoint leftKeypoint = keypoints[connectedParts.first];
            Keypoint rightKeypoint = keypoints[connectedParts.second];

            if (leftKeypoint.getScore() >= keypointThreshold && rightKeypoint.getScore() >= keypointThreshold) {
                connectedKeypoints.add(new Pair<>(leftKeypoint, rightKeypoint));
            }
        }

        return connectedKeypoints;
    }

    /**
     * Draw the Pose on a canvas.
     *
     * @param canvas - the canvas to draw on.
     * @param connectParts - if true, draw lines connecting parts.
     */
    public void draw(Canvas canvas, boolean connectParts) {
        Keypoint[] updatedKeypoints = scaleKeypoints(keypoints, canvas.getWidth(), canvas.getHeight());

        for (Keypoint keypoint : updatedKeypoints) {
            canvas.drawCircle(keypoint.getPosition().x, keypoint.getPosition().y, 5, DrawingUtils.DEFAULT_PAINT);
        }
        if (connectParts) {
            for (Pair<Keypoint, Keypoint> connectedKeypoints : getConnectedKeypoints(updatedKeypoints)) {
                Keypoint left = connectedKeypoints.first;
                Keypoint right = connectedKeypoints.second;
                canvas.drawLine(left.getPosition().x, left.getPosition().y, right.getPosition().x, right.getPosition().y, DrawingUtils.DEFAULT_PAINT);
            }
        }
    }

    /**
     * Draw the Pose on a canvas.
     *
     * @param canvas - the canvas to draw on.
     */
    public void draw(Canvas canvas) {
        draw(canvas, true);
    }

    public JSONArray getKeypointsAsJsonArray() {
        JSONArray keypointsArray = new JSONArray();
        for (Keypoint keypoint : keypoints) {
            keypointsArray.put(keypoint.getPointsAsJsonArray());
        }
        return keypointsArray;
    }

    @Override
    public DataAnnotation toAnnotation(Size sourceInputSize) {
        float scaleX = ((float) sourceInputSize.getWidth()) / bounds.getWidth();
        float scaleY = ((float) sourceInputSize.getHeight()) / bounds.getHeight();

        List keypointAnnotations = new ArrayList();
        for (Keypoint keypoint : keypoints) {
            Keypoint scaledKeypoint = keypoint.scaled(sourceInputSize);
            keypointAnnotations.add(new KeypointAnnotation(
                    keypoint.getId(),
                    keypoint.getName(),
                    keypoint.getPosition().x * scaleX,
                    keypoint.getPosition().y * scaleY, true));
        }
        return new DataAnnotation(skeleton.getLabel(), keypointAnnotations, null, null,false);
    }
}
