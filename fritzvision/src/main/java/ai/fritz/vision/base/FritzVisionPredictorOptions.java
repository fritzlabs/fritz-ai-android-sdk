package ai.fritz.vision.base;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import ai.fritz.core.TFLInterpreterOptionBuilder;

/**
 * Base class for predictor options common for all predictors.
 */
public class FritzVisionPredictorOptions implements TFLInterpreterOptionBuilder {

    public boolean useGPU;
    public boolean useNNAPI;
    public int numThreads;

    @Override
    public Interpreter.Options buildInterpreterOptions() {
        Interpreter.Options interpreterOptions = new Interpreter.Options();

        if (useGPU) {
            GpuDelegate.Options delegateOptions = new GpuDelegate.Options();
            delegateOptions.setPrecisionLossAllowed(true);
            interpreterOptions.addDelegate(new GpuDelegate(delegateOptions));
        } else {
            // Never use this with the GPU option. GPU takes precedence.
            interpreterOptions.setUseNNAPI(useNNAPI);
        }

        interpreterOptions.setNumThreads(numThreads);

        return interpreterOptions;
    }

    public FritzVisionPredictorOptions() {
        useGPU = false;
        useNNAPI = false;
        numThreads = Runtime.getRuntime().availableProcessors();
    }
}
