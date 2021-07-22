package ai.fritz.vision.poseestimation;

import android.graphics.PointF;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Keep track of the displacements (forward and backwards) from the Pose Estimation model output.
 * @hide
 */
public class Displacements {

    private ByteBuffer rawDisplacements;

    private int numEdges;
    private int height;
    private int width;

    public Displacements(ByteBuffer rawDisplacements, int height, int width, int numEdges) {
        this.rawDisplacements = rawDisplacements;
        this.numEdges = numEdges;
        this.height = height;
        this.width = width;
    }

    public int getNumEdges() {
        return numEdges;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public float getDisplacementX(int edgeId, int x, int y) {
        return rawDisplacements.getFloat(4 * (y * width * numEdges * 2 + x * numEdges * 2 + (edgeId + numEdges)));
    }

    public float getDisplacementY(int edgeId, int x, int y) {
        return rawDisplacements.getFloat(4 * (y * width * numEdges * 2 + x * numEdges * 2 + edgeId));

    }

    public PointF getDisplacement(int edgeId, int x, int y) {
        float displacementX = getDisplacementX(edgeId, x, y);
        float displacementY = getDisplacementY(edgeId, x, y);

        return new PointF(displacementX, displacementY);
    }
}
