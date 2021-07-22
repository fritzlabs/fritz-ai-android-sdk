package ai.fritz.visionCV.rigidpose;
import android.graphics.PointF;
import java.nio.ByteBuffer;

/**
 * This class wraps around the offsets specified in the pose estimation model output.
 *
 * @hide
 */
public class Offsets {

    private ByteBuffer rawOffsets;
    private int numParts;
    private int height;
    private int width;

    public Offsets(ByteBuffer rawOffsets, int height, int width, int numParts) {
        this.rawOffsets = rawOffsets;
        this.numParts = numParts;
        this.height = height;
        this.width = width;
    }

    public int getNumParts() {
        return numParts;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public float getOffsetX(int partId, int x, int y) {
        return rawOffsets.getFloat(4 * (y * width * numParts * 2 + x * numParts * 2 + partId));
    }

    public float getOffsetY(int partId, int x, int y) {
        return rawOffsets.getFloat(4 * (y * width * numParts * 2 + x * numParts * 2 + partId + numParts));
    }

    public PointF getOffsetPoint(int partId, int x, int y) {
        float offsetX = getOffsetX(partId, x, y);
        float offsetY = getOffsetY(partId, x, y);
        return new PointF(offsetX, offsetY);
    }
}
