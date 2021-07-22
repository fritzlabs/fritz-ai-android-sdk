package ai.fritz.aistudio.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import ai.fritz.aistudio.R;

// Adapted from https://github.com/veritas1/vertical-slide-color-picker
public class ColorSlider extends View {

    private Paint paint;
    private Paint strokePaint;
    private Path path;
    private int[] colors;

    private Bitmap viewState;
    private int selectorY;
    private int centerX;
    private int sliderRadius;
    private RectF sliderBody;
    private ColorSlider.OnColorChangeListener onColorChangeListener;

    private final int BORDER_WIDTH = 10;
    private int BORDER_COLOR = Color.WHITE;
    private boolean cacheViewState = true;

    public ColorSlider(Context context) {
        super(context);
        initDrawables();
    }

    public ColorSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDrawables();
    }

    public ColorSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDrawables();
    }

    public ColorSlider(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initDrawables();
    }

    /**
     * Set up resources used to draw.
     */
    private void initDrawables() {
        setWillNotDraw(false);
        colors = getResources().getIntArray(R.array.default_colors);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        path = new Path();
        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(BORDER_COLOR);
        strokePaint.setStrokeWidth(BORDER_WIDTH);
        strokePaint.setAntiAlias(true);
        setDrawingCacheEnabled(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        path.addCircle(centerX, BORDER_WIDTH + sliderRadius, sliderRadius, Path.Direction.CW);
        path.addRect(sliderBody, Path.Direction.CW);
        path.addCircle(centerX, getHeight() - (BORDER_WIDTH + sliderRadius), sliderRadius, Path.Direction.CW);
        canvas.drawPath(path, strokePaint);
        canvas.drawPath(path, paint);
        canvas.drawLine(sliderBody.left, selectorY, sliderBody.right, selectorY, strokePaint);
        path.reset();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        centerX = getWidth() / 2;
        sliderRadius = getWidth() / 2 - BORDER_WIDTH;
        sliderBody = new RectF(
                centerX - sliderRadius,
                BORDER_WIDTH + sliderRadius,
                centerX + sliderRadius,
                getHeight() - (BORDER_WIDTH + sliderRadius));
        LinearGradient gradient = new LinearGradient(
                0,
                sliderBody.top,
                0,
                sliderBody.bottom,
                colors,
                null,
                Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        resetToDefault();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (cacheViewState) {
            viewState = getDrawingCache();
            cacheViewState = false;
        }
        selectorY = (int) Math.max(sliderBody.top, Math.min(event.getY(), sliderBody.bottom));
        int selectedColor = viewState.getPixel(getWidth() / 2, selectorY + BORDER_WIDTH);
        if (onColorChangeListener != null) {
            onColorChangeListener.onColorChange(selectedColor);
        }
        invalidate();
        return true;
    }

    /**
     * Set the slider to its initial position and color
     */
    public void resetToDefault() {
        selectorY = BORDER_WIDTH + sliderRadius;
        if (onColorChangeListener != null) {
            onColorChangeListener.onColorChange(0);
        }
        invalidate();
    }

    /**
     * Set a listener for color changes.
     *
     * @param onColorChangeListener the listener
     */
    public void setOnColorChangeListener(ColorSlider.OnColorChangeListener onColorChangeListener) {
        this.onColorChangeListener = onColorChangeListener;
    }

    public interface OnColorChangeListener {
        void onColorChange(int color);
    }
}
