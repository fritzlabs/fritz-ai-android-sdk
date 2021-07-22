package ai.fritz.aistudio.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ai.fritz.aistudio.R;
import ai.fritz.aistudio.ml.Classifier.Recognition;
import ai.fritz.vision.FritzVisionLabel;
import androidx.core.content.ContextCompat;

public class RecognitionScoreView extends View implements ResultsView {
    private static final float TEXT_SIZE_DIP = 24;
    private List<Recognition> results = new ArrayList<>();
    private List<FritzVisionLabel> labels = new ArrayList<>();
    private final float textSizePx;
    private final Paint fgPaint;
    private final Paint bgPaint;

    public RecognitionScoreView(final Context context, final AttributeSet set) {
        super(context, set);

        textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        fgPaint = new Paint();
        fgPaint.setTextSize(textSizePx);
        fgPaint.setColor(ContextCompat.getColor(context, R.color.textColorPrimary));

        bgPaint = new Paint();
        bgPaint.setColor(Color.TRANSPARENT);
    }

    @Override
    public void setResults(final List<Recognition> results) {
        this.results = results;
        postInvalidate();
    }

    @Override
    public void setResult(final List<FritzVisionLabel> labels) {
        this.labels = labels;
        postInvalidate();
    }

    @Override
    public void onDraw(final Canvas canvas) {
        final int x = 10;
        int y = (int) (fgPaint.getTextSize() * 1.5f);

        canvas.drawPaint(bgPaint);

        if (results.size() > 0) {
            for (final Recognition result : results) {
                double confidence = Math.round(result.getConfidence() * 1000) / 10.0;
                canvas.drawText(result.getTitle() + ": " + confidence + "%", x, y, fgPaint);
                y += fgPaint.getTextSize() * 1.5f;
            }
        }

        if (labels.size() > 0) {
            for (final FritzVisionLabel label : labels) {
                double confidence = Math.round(label.getConfidence() * 1000) / 10.0;
                canvas.drawText(label.getText() + ": " + confidence + "%", x, y, fgPaint);
                y += fgPaint.getTextSize() * 1.5f;
            }
        }
    }
}
