package ai.fritz.vision.styletransfer;

import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.core.OutputTensor;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.ImageInputTensor;
import ai.fritz.vision.base.FritzVisionPredictor;

/**
 * FritzVisionStylePredictor takes in a {@link FritzVisionImage} and outputs a new {@link FritzVisionImage} in the specified {@link PaintingManagedModels}.
 */
public class FritzVisionStylePredictor extends FritzVisionPredictor {

    private static final String TAG = FritzVisionStylePredictor.class.getSimpleName();

    private ImageInputTensor imageInputTensor = new ImageInputTensor("Image Input", 0);
    private OutputTensor outputTensor = new OutputTensor("Stylized Image", 0);
    private FritzVisionStylePredictorOptions options;

    public FritzVisionStylePredictor(FritzOnDeviceModel fritzOnDeviceModel, FritzVisionStylePredictorOptions options) {
        super(fritzOnDeviceModel, options);
        this.options = options;
        imageInputTensor.setupInputBuffer(interpreter);
        inputSize = imageInputTensor.getImageDimensions();
        outputTensor.setupOutputBuffer(interpreter);
    }

    /**
     * Applies a style on an image and transforms it into a work of art.
     *
     * @param visionImage The image to run inference on.
     * @return {@link FritzVisionStyleResult}
     */
    public FritzVisionStyleResult predict(FritzVisionImage visionImage) {
        imageInputTensor.preprocess(visionImage);
        interpreter.run(imageInputTensor.buffer, outputTensor.buffer);

        int[] pixels = postprocess();

        return new FritzVisionStyleResult(pixels, inputSize, visionImage.getSize(), options.resize);
    }

    private int[] postprocess() {
        int width = inputSize.getWidth();
        int height = inputSize.getHeight();
        int[] output = new int[width * height];
        outputTensor.buffer.rewind();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                float rValue = outputTensor.getFloat3D(row, col, 0);
                float gValue = outputTensor.getFloat3D(row, col, 1);
                float bValue = outputTensor.getFloat3D(row, col, 2);
                // Alpha (255 shift 24) + R (shift 16) + B (shift 8) + G
                int pixel = (0xFF << 24) + (((int) rValue & 0xFF) << 16) + (((int) gValue & 0xFF) << 8) + ((int) bValue & 0xFF);

                output[row * width + col] = pixel;
            }
        }

        return output;
    }
}