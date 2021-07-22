package ai.fritz.visionCV.rigidpose;

import android.util.Size;

import org.opencv.core.Point;


public class RigidPoseResult {

    private static final String TAG = RigidPoseResult.class.getSimpleName();

    private Point[] keypoints;
    private float[] scores;

    private Size gridSize;

    public RigidPoseResult(Point[] keypoints, float[] maxScoresForParts, Size gridSize) {
        super();
        this.keypoints = keypoints;
        this.scores = maxScoresForParts;
        this.gridSize = gridSize;
    }

    public Point[] getKeypoints() {
        return keypoints;
    }

    public Point[] getScaledKeypoints(Size desiredDimensions) {
        float scaledSizeX = (float) desiredDimensions.getWidth() / gridSize.getWidth();
        float scaledSizeY = (float) desiredDimensions.getHeight() / gridSize.getHeight();

        // set the obj 3D points
        Point[] scaledPoints = new Point[keypoints.length];

        for (int i = 0; i < keypoints.length; i++) {
            Point keypoint = keypoints[i];
            float x = (float) keypoint.x * scaledSizeX;
            float y = (float) keypoint.y * scaledSizeY;
            scaledPoints[i] = new Point(x, y);
        }

        return scaledPoints;
    }

    public Size getGridSize() {
        return gridSize;
    }

    public void setKeypoints(Point[] points) {
        this.keypoints = points;
    }

    public float[] getScores() {
        return scores;
    }
}
