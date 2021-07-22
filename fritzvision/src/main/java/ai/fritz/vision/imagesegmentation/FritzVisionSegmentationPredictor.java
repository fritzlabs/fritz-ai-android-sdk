package ai.fritz.vision.imagesegmentation;

import android.util.Size;

import org.tensorflow.lite.Tensor;

import java.util.List;

import ai.fritz.core.OutputTensor;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.ImageInputTensor;
import ai.fritz.vision.base.FritzVisionRecordablePredictor;

/**
 * The predictor for image segmentation models.
 */
public class FritzVisionSegmentationPredictor extends FritzVisionRecordablePredictor {

    private static final String TAG = FritzVisionSegmentationPredictor.class.getSimpleName();

    private Size outputSize;

    private MaskClass[] segmentClassifications;
    private FritzVisionSegmentationPredictorOptions options;

    private ImageInputTensor inputTensor = new ImageInputTensor("Image Input", 0);
    private OutputTensor outputTensor = new OutputTensor("SegmentationOutput", 0);

    public FritzVisionSegmentationPredictor(SegmentationOnDeviceModel segmentationOnDeviceModel, FritzVisionSegmentationPredictorOptions options) {
        super(segmentationOnDeviceModel, options);

        this.segmentClassifications = setTargetClassifications(segmentationOnDeviceModel.getMaskClasses(), options.targetClasses);
        this.options = options;
        initializeBuffers();
    }

    private void initializeBuffers() {
        inputTensor.setupInputBuffer(interpreter);
        inputSize = inputTensor.getImageDimensions();

        outputTensor.setupOutputBuffer(interpreter);
        outputSize = outputTensor.getBounds();
    }

    private boolean tensorSizeChanged() {
        Tensor inputTensor = interpreter.getInputTensor(0);
        if (!getSizeFromTensor(inputTensor).equals(inputSize)) {
            return true;
        }
        Tensor outputTensor = interpreter.getOutputTensor(0);
        return !getSizeFromTensor(outputTensor).equals(outputSize);
    }

    private MaskClass[] setTargetClassifications(MaskClass[] classifications, List<MaskClass> targetSegments) {
        // If no target segments set, then use the default
        if (targetSegments == null) {
            return classifications;
        }

        // Filter out the classes outside of the target segments
        for (int i = 0; i < classifications.length; i++) {
            MaskClass maskClass = classifications[i];
            if (!targetSegments.contains(maskClass)) {
                classifications[i] = MaskClass.NONE;
            }
        }

        return classifications;
    }

    /**
     * Identify and create pixel-level masks for all items in visionImage.
     *
     * @param visionImage The image to run inference on.
     * @return {@link FritzVisionSegmentationResult}
     */
    public FritzVisionSegmentationResult predict(FritzVisionImage visionImage) {
        if (tensorSizeChanged()) {
            initializeBuffers();
        }
        inputTensor.preprocess(visionImage, DEFAULT_PREPROCESSING_PARAMS);
        interpreter.run(inputTensor.buffer, outputTensor.buffer);
        FritzVisionSegmentationResult result = postprocess();
        return result;
    }

    private FritzVisionSegmentationResult postprocess() {
        int[][] classifications = new int[outputSize.getHeight()][outputSize.getWidth()];
        float[][] confidence = new float[outputSize.getHeight()][outputSize.getWidth()];
        int height = outputSize.getHeight();
        int width = outputSize.getWidth();
        outputTensor.rewind();

        for (int row = 0; row < height; row++) {
            int rowOffset = row * width * segmentClassifications.length;

            for (int col = 0; col < width; col++) {
                int maxClassProbIndex = 0;
                float maxClassProbValue = 0;

                int colOffset = col * segmentClassifications.length;
                int offset = rowOffset + colOffset;

                for (int classIndex = 0; classIndex < segmentClassifications.length; classIndex++) {
                    float classProb = outputTensor.getFloat(offset + classIndex);

                    // Arg max
                    if (classProb > maxClassProbValue) {
                        maxClassProbIndex = classIndex;
                        maxClassProbValue = classProb;
                    }
                }

                classifications[row][col] = maxClassProbIndex;
                confidence[row][col] = maxClassProbValue;
            }
        }

        return new FritzVisionSegmentationResult(
                options,
                segmentClassifications,
                inputSize,
                outputSize,
                0,
                0,
                classifications,
                confidence);
    }
}
