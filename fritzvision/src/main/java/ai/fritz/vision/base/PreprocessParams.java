package ai.fritz.vision.base;

/**
 * Params to normalize each pixel channel value for an image.
 */
public class PreprocessParams {
    float mean;
    float std;

    public PreprocessParams(float mean, float std) {
        this.mean = mean;
        this.std = std;
    }

    public float normalize(float channelValue) {
        return (channelValue - mean) / std;
    }
}
