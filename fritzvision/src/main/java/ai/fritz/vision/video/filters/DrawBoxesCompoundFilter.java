package ai.fritz.vision.video.filters;

import android.graphics.Bitmap;

import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictor;
import ai.fritz.vision.objectdetection.FritzVisionObjectResult;
import ai.fritz.vision.video.FritzVisionImageFilter;

/**
 * Filter for drawing boxes around detected objects.
 */
public class DrawBoxesCompoundFilter extends FritzVisionImageFilter {

    private FritzVisionObjectPredictor predictor;

    public DrawBoxesCompoundFilter(FritzVisionObjectPredictor predictor) {
        this.predictor = predictor;
    }

    @Override
    public FilterCompositionMode getCompositionMode() {
        return FilterCompositionMode.COMPOUND_WITH_PREVIOUS_OUTPUT;
    }

    @Override
    public FritzVisionImage processImage(FritzVisionImage image) {
        FritzVisionObjectResult objectResult = predictor.predict(image);
        Bitmap boxes = image.overlayBoundingBoxes(objectResult.getObjects());
        return FritzVisionImage.fromBitmap(boxes);
    }
}
