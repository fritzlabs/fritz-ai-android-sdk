package ai.fritz.vision.imagelabeling;

import android.util.Size;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ai.fritz.core.OutputTensor;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionLabel;
import ai.fritz.vision.ImageInputTensor;
import ai.fritz.vision.base.FritzVisionRecordablePredictor;

/**
 * Label the contents of an image.
 * <p>
 * <a href="https://docs.fritz.ai/features/image-labeling/about.html">Learn more</a>
 */
public class FritzVisionLabelPredictor extends FritzVisionRecordablePredictor {

    private static final String TAG = FritzVisionLabelPredictor.class.getSimpleName();

    private ImageInputTensor inputTensor = new ImageInputTensor("Input Image", 0);
    private OutputTensor outputTensor = new OutputTensor("Image Label Output", 0);

    /**
     * Set the options for the predictor
     */
    private FritzVisionLabelPredictorOptions options;
    private List<String> labels;

    public FritzVisionLabelPredictor(LabelingOnDeviceModel onDeviceModel, FritzVisionLabelPredictorOptions options) {
        super(onDeviceModel, options);
        this.options = options;

        this.labels = onDeviceModel.getLabels();

        inputTensor.setupInputBuffer(interpreter);
        outputTensor.setupOutputBuffer(interpreter);

        inputSize = inputTensor.getImageDimensions();
    }

    /**
     * Get the size of the model's input tensor.
     *
     * @return the input size.
     */
    public Size getInputSize() {
        return inputSize;
    }

    /**
     * Get the labels used for the model.
     *
     * @return a list of all labels.
     */
    public List<String> getLabels() {
        return labels;
    }

    /**
     * Given an image, predicts a classification based on the image.
     *
     * @param visionImage The image to run inference on.
     * @return {@link FritzVisionLabelResult}
     */
    public FritzVisionLabelResult predict(FritzVisionImage visionImage) {
        inputTensor.preprocess(visionImage);
        outputTensor.rewind();
        interpreter.run(inputTensor.buffer, outputTensor.buffer);
        return new FritzVisionLabelResult(getLabelResults());
    }


    private List<FritzVisionLabel> getLabelResults() {
        // Copy the label list (note the label objects are references. not copies)
        List<FritzVisionLabel> labelsPastThreshold = new ArrayList<>();

        // Calculate the confidence for each label
        for (int i = 0; i < labels.size(); i++) {
            String labelText = labels.get(i);
            float confidenceScore = getNormalizedProbability(i);
            if (confidenceScore >= options.confidenceThreshold) {
                labelsPastThreshold.add(new FritzVisionLabel(labelText, confidenceScore));
            }
        }

        // Sort the list in descending order
        Collections.sort(labelsPastThreshold, new Comparator<FritzVisionLabel>() {
            @Override
            public int compare(FritzVisionLabel vl1, FritzVisionLabel vl2) {
                return Float.compare(vl2.getConfidence(), vl1.getConfidence());
            }
        });
        return labelsPastThreshold;
    }

    protected float getNormalizedProbability(int labelIndex) {
        if (outputTensor.is8BitQuantized()) {
            return (outputTensor.getByte(labelIndex) & 0xff) / 255.0f;
        }

        return outputTensor.getFloat(labelIndex);
    }
}

