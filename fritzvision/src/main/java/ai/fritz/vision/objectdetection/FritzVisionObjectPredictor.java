package ai.fritz.vision.objectdetection;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.fritz.core.OutputTensor;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionLabel;
import ai.fritz.vision.FritzVisionObject;
import ai.fritz.vision.ImageInputTensor;
import ai.fritz.vision.base.FritzVisionRecordablePredictor;

/**
 * Predicts the different objects that are found in an image.
 * <p>
 * <a href="https://docs.fritz.ai/features/object-detection/about.html">Learn more</a>
 */
public class FritzVisionObjectPredictor extends FritzVisionRecordablePredictor {

    private static final String TAG = FritzVisionObjectPredictor.class.getSimpleName();
    private static final int NUM_DETECTIONS = 10;
    private List<String> labels;

    private ImageInputTensor inputTensor = new ImageInputTensor("Input Image", 0);

    private OutputTensor locations = new OutputTensor("Box Coordinates", 0);
    private OutputTensor classes = new OutputTensor("Class Predictions", 1);
    private OutputTensor scores = new OutputTensor("Prediction Scores", 2);
    private OutputTensor numDetections = new OutputTensor("Num Detections", 3);

    private OutputTensor[] allOutputs = {locations, classes, scores, numDetections};
    private FritzVisionObjectPredictorOptions options;

    // Model inference config
    // Indexes mapping the box output to [xmin, ymin, xmax, ymax]
    Integer[] boxIndices;
    boolean isOutputNormalized;

    public FritzVisionObjectPredictor(ObjectDetectionOnDeviceModel onDeviceModel, FritzVisionObjectPredictorOptions options) {
        super(onDeviceModel, options);
        this.options = options;

        this.boxIndices = onDeviceModel.getBoxIndices();
        this.isOutputNormalized = onDeviceModel.isOutputNormalized();
        this.labels = onDeviceModel.getLabels();

        inputTensor.setupInputBuffer(interpreter);
        this.inputSize = inputTensor.getImageDimensions();

        for (OutputTensor output: allOutputs) {
            output.setupOutputBuffer(interpreter);
        }
    }

    /**
     * Detect objects in a given image.
     * <p>
     * Given an image, tries to identify over 100 objects in it.
     * <p>
     * 1. Center crop the image
     * 2. Resize the cropped image to 300x300 to pass into the model.
     * 3. Run inference on the image
     * 4. Get the bounding box coordinates for the cropped image.
     * 5. Get the bounding box coordinates for the original image.
     *
     * @param visionImage The image to run inference on.
     * @return {@link FritzVisionObjectResult}
     */
    @Override
    public FritzVisionObjectResult predict(FritzVisionImage visionImage) {
        inputTensor.preprocess(visionImage, DEFAULT_PREPROCESSING_PARAMS);
        rewindOutputs();

        Object[] inputArray = {inputTensor.buffer};
        Map<Integer, Object> outputMap = new HashMap<>();
        for (OutputTensor output : allOutputs) {
            outputMap.put(output.getTensorIndex(), output.buffer);
        }
        interpreter.runForMultipleInputsOutputs(inputArray, outputMap);

        List<FritzVisionObject> visionObjects = postprocess();
        return new FritzVisionObjectResult(visionObjects, options.confidenceThreshold, visionImage.encodedSize());
    }

    private void rewindOutputs() {
        for (OutputTensor output : allOutputs) {
            output.rewind();
        }
    }

    private List<FritzVisionObject> postprocess() {
        // Show the best detections.
        final ArrayList<FritzVisionObject> visionObjects = new ArrayList<>();
        for (int i = 0; i < NUM_DETECTIONS; ++i) {

            float xMin = locations.getFloat2D(i, boxIndices[0]);
            float yMin = locations.getFloat2D(i, boxIndices[1]);
            float xMax = locations.getFloat2D(i, boxIndices[2]);
            float yMax = locations.getFloat2D(i, boxIndices[3]);

            if (this.isOutputNormalized) {
                xMin *= inputSize.getWidth();
                yMin *= inputSize.getHeight();
                xMax *= inputSize.getWidth();
                yMax *= inputSize.getHeight();
            }

            float confidence = scores.getFloat(i);
            float outputClassIndex = classes.getFloat(i);
            final RectF detection = new RectF(xMin, yMin, xMax, yMax);
            if (confidence < options.confidenceThreshold) {
                continue;
            }
            // Class labels start from 1 to number_of_classes+1,
            String labelText = labels.get((int) outputClassIndex + 1);
            FritzVisionLabel label = new FritzVisionLabel(labelText, confidence);
            visionObjects.add(new FritzVisionObject(label, detection, inputSize));
        }

        List<FritzVisionObject> selectedObjects = new ArrayList<>();

        for (FritzVisionObject objectA : visionObjects) {
            boolean shouldSelect = true;

            String textToCompare = objectA.getVisionLabel().getText();

            // Does the current box overlap one of the selected boxes more than the
            // given threshold amount? Then it's too similar, so don't keep it.
            for (FritzVisionObject objectB : selectedObjects) {
                if (!textToCompare.equalsIgnoreCase(objectB.getVisionLabel().getText())) {
                    continue;
                }
                float overlapScore = IOU(objectA.getBoundingBox(), objectB.getBoundingBox());
                if (overlapScore > options.iouThreshold) {
                    shouldSelect = false;
                    break;
                }
            }

            if (shouldSelect) {
                selectedObjects.add(objectA);
            }
        }

        return selectedObjects;
    }

    private float IOU(RectF boxA, RectF boxB) {
        float areaA = (boxA.right - boxA.left) * (boxA.top - boxA.bottom);
        float areaB = (boxB.right - boxB.left) * (boxB.top - boxB.bottom);

        float intersectionMinX = Math.max(boxA.left, boxB.left);
        float intersectionMaxX = Math.min(boxA.right, boxB.right);
        float intersectionMaxY = Math.min(boxA.top, boxB.top);
        float intersectionMinY = Math.max(boxA.bottom, boxB.bottom);

        float intersectionArea = Math.max(intersectionMaxY - intersectionMinY, 0) *
                Math.max(intersectionMaxX - intersectionMinX, 0);
        return intersectionArea / (areaA + areaB - intersectionArea);
    }
}
