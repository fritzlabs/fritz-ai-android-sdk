package ai.fritz.vision.poseestimation;

/**
 * This class wraps around the Parts / Keypoints in the pose.
 *
 * @hide
 */
public class Part {
    private int heatMapScoresX;
    private int heatMapScoresY;
    private int keypointId;

    public Part(int keypointId, int x, int y){
        this.keypointId = keypointId;
        this.heatMapScoresX = x;
        this.heatMapScoresY = y;
    }

    public int getHeatMapScoresX() {
        return heatMapScoresX;
    }

    public int getHeatMapScoresY() {
        return heatMapScoresY;
    }

    public int getKeypointId() {
        return keypointId;
    }
}
