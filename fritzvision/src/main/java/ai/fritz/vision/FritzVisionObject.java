package ai.fritz.vision;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Size;

import java.util.ArrayList;
import java.util.Locale;

import ai.fritz.core.annotations.AnnotatableObject;
import ai.fritz.core.annotations.BoundingBoxAnnotation;
import ai.fritz.core.annotations.KeypointAnnotation;
import ai.fritz.core.annotations.DataAnnotation;
import ai.fritz.vision.base.DrawingUtils;

/**
 * FritzVisionObject is a standard output class for FritzVisionObjectPredictor.
 *
 * <p>
 * This object holds the output for Object Detection.
 * <p>
 * It contains a {@link FritzVisionLabel} and a bounding box.
 */
public class FritzVisionObject implements AnnotatableObject {

    private static final String TAG = FritzVisionObject.class.getSimpleName();

    // These are used for drawing the text relative to the bounding box.
    private static final int LEFT_OFFSET = 8;
    private static final int TOP_OFFSET = 32;

    private FritzVisionLabel visionLabel;
    private RectF boundingBox;
    private Size bounds;

    public FritzVisionObject(String text, float confidence, RectF boundingBox, Size bounds) {
        this.visionLabel = new FritzVisionLabel(text, confidence);
        this.boundingBox = boundingBox;
        this.bounds = bounds;
    }

    public FritzVisionObject(FritzVisionLabel visionLabel, RectF boundingBox, Size bounds) {
        this.visionLabel = visionLabel;
        this.boundingBox = boundingBox;
        this.bounds = bounds;
    }

    public FritzVisionLabel getVisionLabel() {
        return visionLabel;
    }

    public RectF getBoundingBox() {
        return boundingBox;
    }

    /**
     * Update the bounding box location for a given grid size.
     * <p>
     * By default, the bounding box coordinates are set relative to the model input dimensions. To update the coordinates,
     * use this method by passing in the desired size.
     * <p>
     * e.g Model Input 224x224
     * Bounding Box Location: left: 1, top: 10, right: 10, bottom: 1
     * <p>
     * scaledTo called with size: 448x448
     * New Bounding Box Location: left: 2, top: 20, right: 20, bottom: 2
     *
     * @param newBounds - the new bounds
     * @return FritzVisionObject
     */
    public FritzVisionObject scaledTo(Size newBounds) {
        float scaleX = ((float) newBounds.getWidth()) / bounds.getWidth();
        float scaleY = ((float) newBounds.getHeight()) / bounds.getHeight();
        RectF newBoundingBox = new RectF(boundingBox.left * scaleX, boundingBox.top * scaleY, boundingBox.right * scaleX, boundingBox.bottom * scaleY);

        return new FritzVisionObject(visionLabel, newBoundingBox, newBounds);
    }

    /**
     * Draw the bounding box and associated text on the canvas.
     *
     * @param canvas - the canvas to draw on
     */
    public void draw(Canvas canvas) {
        float scaleX = ((float) canvas.getWidth()) / bounds.getWidth();
        float scaleY = ((float) canvas.getHeight()) / bounds.getHeight();
        RectF newBoundingBox = new RectF(boundingBox.left * scaleX, boundingBox.top * scaleY, boundingBox.right * scaleX, boundingBox.bottom * scaleY);

        String text = String.format(Locale.ENGLISH, "%s %.2f", visionLabel.getText(), visionLabel.getConfidence());
        canvas.drawRect(newBoundingBox, DrawingUtils.DEFAULT_PAINT);
        canvas.drawText(text, newBoundingBox.left + LEFT_OFFSET, newBoundingBox.top + TOP_OFFSET, DrawingUtils.DEFAULT_TEXT_PAINT);
    }

    @Override
    public DataAnnotation toAnnotation(Size sourceInputSize) {
        RectF boundingBox = getBoundingBox();

        float scaleX = ((float) sourceInputSize.getWidth()) / bounds.getWidth();
        float scaleY = ((float) sourceInputSize.getHeight()) / bounds.getHeight();
        RectF newBoundingBox = new RectF(boundingBox.left * scaleX, boundingBox.top * scaleY, boundingBox.right * scaleX, boundingBox.bottom * scaleY);

        float width = newBoundingBox.right - newBoundingBox.left;
        float height = newBoundingBox.top - newBoundingBox.bottom;
        BoundingBoxAnnotation bbox = new BoundingBoxAnnotation(newBoundingBox.left, newBoundingBox.bottom, width, height);
        return new DataAnnotation(visionLabel.getText(), new ArrayList<KeypointAnnotation>(), bbox, null, false);
    }
}
