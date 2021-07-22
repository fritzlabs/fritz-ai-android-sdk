package ai.fritz.visionCV.rigidpose;
import java.nio.ByteBuffer;

/**
 * Wraps around the keypoint scores from the pose estimation model output.
 *
 * @hide
 */
public class HeatmapScores {

    private ByteBuffer rawScores;

    private int numKeypoints;
    private int height;
    private int width;

    public HeatmapScores(ByteBuffer rawScores, int height, int width, int numKeypoints) {
        this.rawScores = rawScores;
        this.numKeypoints = numKeypoints;
        this.height = height;
        this.width = width;
    }

    public float getScore(int partId, int x, int y) {
        return rawScores.getFloat(4 * (y * width * numKeypoints + x * numKeypoints + partId));
    }

    public int getNumKeypoints() {
        return numKeypoints;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
