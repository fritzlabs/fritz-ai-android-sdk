package ai.fritz.vision.styletransfer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Size;

import ai.fritz.core.utils.BitmapUtils;
import ai.fritz.vision.FritzVisionImage;

/**
 * FritzVisionStyleResult contains the input and output from {@link FritzVisionStylePredictor#predict(FritzVisionImage)}.
 */
public class FritzVisionStyleResult {

    private int[] modelOutputPixels;
    private Size inputImageSize;
    private Size outputImageSize;
    private boolean resize;

    public FritzVisionStyleResult(int[] modelOutputPixels, Size inputImageSize, Size outputImageSize, boolean resize) {
        this.modelOutputPixels = modelOutputPixels;
        this.inputImageSize = inputImageSize;
        this.outputImageSize = outputImageSize;
        this.resize = resize;
    }

    /**
     * Get the stylized bitmap.
     *
     * If the resize option was set, then the resulting bitmap will have the same dimensions as the image passed in.
     *
     * @return a bitmap
     */
    public Bitmap toBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(modelOutputPixels, inputImageSize.getWidth(), inputImageSize.getHeight(), Bitmap.Config.ARGB_8888);
        if (resize) {
            return BitmapUtils.resize(bitmap, outputImageSize.getWidth(), outputImageSize.getHeight());
        }

        return bitmap;
    }

    /**
     * Get the styled bitmap resized to match the target size.
     *
     * @return a bitmap
     */
    public Bitmap toBitmap(Size targetSize) {
        Bitmap bitmap = Bitmap.createBitmap(modelOutputPixels, inputImageSize.getWidth(), inputImageSize.getHeight(), Bitmap.Config.ARGB_8888);
        return BitmapUtils.resize(bitmap, targetSize.getWidth(), targetSize.getHeight());
    }

    /**
     * Draw the output to the canvas.
     *
     * @param canvas
     */
    public void drawToCanvas(Canvas canvas) {
        drawToCanvas(canvas);
    }

    /**
     * Draw the output to the canvas and resize the image.
     *
     * @param canvas
     * @param canvasSize
     */
    public void drawToCanvas(Canvas canvas, Size canvasSize) {
        Bitmap bitmap = toBitmap();
        Matrix matrix = new Matrix();
        float scaleWidth = (float) canvasSize.getWidth() / bitmap.getWidth();
        float scaleHeight = (float) canvasSize.getHeight() / bitmap.getHeight();
        matrix.postScale(scaleWidth, scaleHeight);
        canvas.drawBitmap(bitmap, matrix, new Paint());
    }

}
