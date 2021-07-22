package ai.fritz.vision.imagesegmentation;

import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import ai.fritz.vision.ProcessingContext;


/**
 * @hide
 */
public class MaskPostProcessOptions {
    private Bitmap mask;
    private RenderScript rs;

    public MaskPostProcessOptions(Bitmap mask) {
        this.mask = mask;
        this.rs = ProcessingContext.getInstance().getRS();
    }

    /**
     * Blur the edges of a mask.
     *
     * @param radius the extent of the blur
     */
    public void addBlur(float radius) {
        Bitmap outputBitmap = Bitmap.createBitmap(mask);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        Allocation tmpIn = Allocation.createFromBitmap(rs, mask);

        ScriptIntrinsicBlur filter = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        filter.setRadius(radius);
        filter.setInput(tmpOut);
        filter.forEach(tmpIn);
        tmpIn.copyTo(outputBitmap);
    }
}
