package ai.fritz.vision;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;

import ai.fritz.vision.imagesegmentation.BlendMode;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSoftLightBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageTwoInputFilter;
import jp.co.cyberagent.android.gpuimage.util.Rotation;

public class FritzSurfaceView extends GPUImageView {

    private static final String TAG = FritzSurfaceView.class.getSimpleName();

    public FritzSurfaceView(Context context) {
        super(context);
    }

    public FritzSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void drawBlendedMask(FritzVisionImage visionImage, Bitmap mask, BlendMode blendMode) {
        drawBlendedMask(visionImage, mask, blendMode, false);
    }

    public void drawBlendedMask(FritzVisionImage visionImage, Bitmap mask, BlendMode blendMode, boolean mirrored) {
        long start = System.currentTimeMillis();
        Bitmap bitmap = visionImage.buildOrientedBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), false);
        Bitmap resizedMaskBitmap = Bitmap.createScaledBitmap(mask, getWidth(), getHeight(), false);
        GPUImageTwoInputFilter blendFilter = getGPUFilter(blendMode);
        blendFilter.setBitmap(resizedMaskBitmap);
        setImage(bitmapResized);
        setFilter(blendFilter);

        if (mirrored) {
            blendFilter.setRotation(Rotation.ROTATION_180, false, true);
            getGPUImage().setRotation(Rotation.ROTATION_180, false, true);
        } else {
            getGPUImage().setRotation(Rotation.NORMAL, false, false);
        }
        Log.d(TAG, "Blend Time: " + (System.currentTimeMillis() - start));
    }

    private GPUImageTwoInputFilter getGPUFilter(BlendMode blendMode) {
        if (blendMode == BlendMode.HUE) {
            return new GPUImageHueBlendFilter();
        }

        if (blendMode == BlendMode.COLOR) {
            return new GPUImageColorBlendFilter();
        }

        return new GPUImageSoftLightBlendFilter();
    }
}
