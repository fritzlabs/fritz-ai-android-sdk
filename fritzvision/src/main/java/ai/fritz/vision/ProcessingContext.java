package ai.fritz.vision;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicResize;
import android.renderscript.Type;
import android.util.Size;

import ai.fritz.core.Fritz;
import ai.fritz.vision.rs.ScriptC_rotator;

/**
 * @hide
 */
public class ProcessingContext {

    private static volatile ProcessingContext instance;

    public static ProcessingContext getInstance() {
        // Use double locking
        if (instance == null) {
            synchronized (ProcessingContext.class) {
                if (instance == null) {
                    instance = new ProcessingContext();
                }
            }
        }

        return instance;
    }

    private RenderScript rs;
    private ScriptC_rotator rotatorScript;
    private ScriptIntrinsicResize resizeScript;

    private ProcessingContext() {
        rs = RenderScript.create(Fritz.getAppContext());
        rotatorScript = new ScriptC_rotator(rs);
        resizeScript = ScriptIntrinsicResize.create(rs);
    }

    public RenderScript getRS() {
        return rs;
    }

    public synchronized Allocation resize(Allocation allocation, Element element, Size targetSize) {
        Type outType = Type.createXY(rs, element, targetSize.getWidth(),
                targetSize.getHeight());
        Allocation allocationOut = Allocation.createTyped(rs, outType);

        resizeScript.setInput(allocation);
        resizeScript.forEach_bicubic(allocationOut);

        return allocationOut;
    }

    public synchronized Allocation rotate(Allocation allocation, Element element, int width, int height, int rotation) {
        rotatorScript.set_inWidth(width);
        rotatorScript.set_inHeight(height);
        rotatorScript.set_inImage(allocation);

        Allocation allocationOut;

        switch (rotation) {
            case 90: {
                Type outType = Type.createXY(rs, element, height, width);
                allocationOut = Allocation.createTyped(rs, outType);
                rotatorScript.forEach_rotate_90_clockwise(allocationOut, allocationOut);
                break;
            }
            case 180: {
                Type outType = Type.createXY(rs, element, width, height);
                allocationOut = Allocation.createTyped(rs, outType);
                rotatorScript.forEach_rotate_180(allocationOut, allocationOut);
                break;
            }
            case 270: {
                Type outType = Type.createXY(rs, element, height, width);
                allocationOut = Allocation.createTyped(rs, outType);
                rotatorScript.forEach_rotate_270_clockwise(allocationOut, allocationOut);
                break;
            }
            default:
                throw new IllegalArgumentException("rotateClockwise() only supports 90 degree increments");
        }

        return allocationOut;
    }

    public synchronized Allocation flipHorizontal(Allocation allocation, Element element, int width, int height) {
        rotatorScript.set_inWidth(width);
        rotatorScript.set_inHeight(height);
        rotatorScript.set_inImage(allocation);

        Allocation allocationOut;

        Type outType = Type.createXY(rs, element, width, height);
        allocationOut = Allocation.createTyped(rs, outType);
        rotatorScript.forEach_flip_horizontal(allocationOut, allocationOut);
        return allocationOut;
    }

    public synchronized Allocation flipVertical(Allocation allocation, Element element, int width, int height) {
        rotatorScript.set_inWidth(width);
        rotatorScript.set_inHeight(height);
        rotatorScript.set_inImage(allocation);

        Allocation allocationOut;

        Type outType = Type.createXY(rs, element, width, height);
        allocationOut = Allocation.createTyped(rs, outType);
        rotatorScript.forEach_flip_vertical(allocationOut, allocationOut);
        return allocationOut;
    }
}
