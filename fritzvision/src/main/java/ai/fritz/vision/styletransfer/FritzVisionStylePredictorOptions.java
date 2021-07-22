package ai.fritz.vision.styletransfer;

import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.base.FritzVisionPredictorOptions;

/**
 * Options that developers can tune in order to specify the style settings.
 * <p>
 * by {{@link FritzVisionStylePredictor#predict(FritzVisionImage)}}
 */
public class FritzVisionStylePredictorOptions extends FritzVisionPredictorOptions {

    public boolean resize;

    public FritzVisionStylePredictorOptions() {
        super();
        this.resize = false;
    }
}

