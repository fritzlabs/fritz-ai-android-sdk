package ai.fritz.vision.base;

import android.util.Size;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.nio.ByteBuffer;

import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.core.FritzTFLiteInterpreter;
import ai.fritz.core.TFLInterpreterOptionBuilder;
import ai.fritz.vision.ByteImage;
import ai.fritz.vision.FritzVisionImage;

/**
 * TFL Predictor
 */
public abstract class FritzVisionPredictor<T> {
    // Normalize between -.5 and .5
    public static final PreprocessParams DEFAULT_PREPROCESSING_PARAMS = new PreprocessParams(128, 255.0f);

    private static final int DEFAULT_HEIGHT_IDX = 1;
    private static final int DEFAULT_WIDTH_IDX = 2;

    protected FritzOnDeviceModel onDeviceModel;
    protected FritzTFLiteInterpreter interpreter;
    protected Size inputSize;

    public FritzVisionPredictor(FritzOnDeviceModel onDeviceModel) {
        this(onDeviceModel, new TFLInterpreterOptionBuilder() {
            @Override
            public Interpreter.Options buildInterpreterOptions() {
                return new Interpreter.Options();
            }
        });
    }

    public FritzVisionPredictor(FritzOnDeviceModel onDeviceModel, TFLInterpreterOptionBuilder optionBuilder) {
        this.onDeviceModel = onDeviceModel;
        this.interpreter = new FritzTFLiteInterpreter(onDeviceModel, optionBuilder);
    }

    public Size getInputSize() {
        return inputSize;
    }

    /**
     * Get the underlying TFL interpreter.
     *
     * @return the unwrapped interpreter.
     */
    public Interpreter getInterpreter() {
        return interpreter.getInterpreter();
    }

    public void close() {
        interpreter.close();
    }

    protected Size getSizeFromTensor(Tensor tensor) {
        int[] inputShape = tensor.shape();
        int inputHeight = inputShape[DEFAULT_HEIGHT_IDX];
        int inputWidth = inputShape[DEFAULT_WIDTH_IDX];
        return new Size(inputWidth, inputHeight);
    }

    public abstract T predict(FritzVisionImage visionImage);
}
