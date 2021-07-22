package ai.fritz.vision.imagesegmentation;

import android.graphics.Color;

public class FritzVisionSegmentationMaskOptions extends FritzVisionSegmentationPredictorOptions {

    // Color of the mask.
    // If unspecified, the mask will default to the color associated with its class.
    public int maskColor;

    // Alpha value of the mask in the range [0, 255]
    // Default value is 255.
    public int maxAlpha;

    // Radius to blur the edges of the mask.
    // Default value is 0.
    public float blurRadius;

    public FritzVisionSegmentationMaskOptions() {
        this.maskColor = Color.TRANSPARENT;
        this.maxAlpha = 255;
        this.blurRadius = 0;
    }
}
