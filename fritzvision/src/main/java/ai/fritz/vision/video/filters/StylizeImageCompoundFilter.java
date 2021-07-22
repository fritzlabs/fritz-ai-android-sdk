package ai.fritz.vision.video.filters;

import android.graphics.Bitmap;

import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.styletransfer.FritzVisionStylePredictor;
import ai.fritz.vision.styletransfer.FritzVisionStyleResult;
import ai.fritz.vision.video.FritzVisionImageFilter;

/**
 * Filter for stylizing the input image.
 */
public class StylizeImageCompoundFilter extends FritzVisionImageFilter {

    private FritzVisionStylePredictor predictor;

    public StylizeImageCompoundFilter(FritzVisionStylePredictor predictor) {
        this.predictor = predictor;
    }

    @Override
    public FilterCompositionMode getCompositionMode() {
        return FilterCompositionMode.COMPOUND_WITH_PREVIOUS_OUTPUT;
    }

    @Override
    public FritzVisionImage processImage(FritzVisionImage image) {
        FritzVisionStyleResult styleResult = predictor.predict(image);
        Bitmap stylized = styleResult.toBitmap();
        return FritzVisionImage.fromBitmap(stylized);
    }
}
