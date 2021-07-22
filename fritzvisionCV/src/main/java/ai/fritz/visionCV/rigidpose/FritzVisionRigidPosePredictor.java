package ai.fritz.visionCV.rigidpose;


import android.graphics.PointF;
import android.util.Size;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Tensor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import ai.fritz.vision.ByteImage;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.base.FritzVisionPredictor;
import ai.fritz.visionCV.FritzCVImage;

/**
 * The predictor class for rigid poses.
 */
public class FritzVisionRigidPosePredictor extends FritzVisionPredictor {

    private static final String TAG = FritzVisionRigidPosePredictor.class.getSimpleName();
    private static final int NUM_CHANNELS = 3;

    private int numKeypoints;

    private static final int INPUT_IDX = 0;
    private static final int HEATMAP_IDX = 0;
    private static final int OFFSETS_IDX = 1;

    private float scoreThreshold;
    private int minPartsOverThreshold;

    private ByteBuffer inputByteBuffer;
    private ByteBuffer outputHeatmaps;
    private ByteBuffer outputOffsets;

    private byte[] inputData;

    private Size outputSize;

    private org.opencv.core.Size inputCVSize;
    private org.opencv.core.Size outputCVSize;

    private FritzVisionRigidPosePredictorOptions options;

    public FritzVisionRigidPosePredictor(RigidPoseOnDeviceModel poseOnDeviceModel) {
        this(poseOnDeviceModel, new FritzVisionRigidPosePredictorOptions());
    }

    public FritzVisionRigidPosePredictor(RigidPoseOnDeviceModel poseOnDeviceModel, FritzVisionRigidPosePredictorOptions options) {
        super(poseOnDeviceModel, options);

        this.options = options;

        this.scoreThreshold = options.confidenceThreshold;
        this.minPartsOverThreshold = options.numKeypointsAboveThreshold;
        numKeypoints = poseOnDeviceModel.getNumKeypoints();

        Tensor inputTensor = interpreter.getInputTensor(INPUT_IDX);
        inputSize = getSizeFromTensor(inputTensor);
        inputCVSize = new org.opencv.core.Size(inputSize.getWidth(), inputSize.getHeight());
        int numBytes = inputTensor.dataType().byteSize();
        this.inputByteBuffer = ByteBuffer.allocateDirect(numBytes * inputTensor.numElements());
        inputByteBuffer.order(ByteOrder.nativeOrder());

        // Only RGB values
        inputData = new byte[inputSize.getHeight() * inputSize.getWidth() * NUM_CHANNELS];

        Tensor outputHeatmapTensor = interpreter.getOutputTensor(HEATMAP_IDX);
        outputSize = getSizeFromTensor(outputHeatmapTensor);
        outputCVSize = new org.opencv.core.Size(outputSize.getWidth(), outputSize.getHeight());
        int outputHeatmapBytes = outputHeatmapTensor.dataType().byteSize();
        outputHeatmaps = ByteBuffer.allocateDirect(outputHeatmapBytes * outputHeatmapTensor.numElements());
        outputHeatmaps.order(ByteOrder.nativeOrder());

        Tensor outputOffsetsTensor = interpreter.getOutputTensor(OFFSETS_IDX);
        int ouputOffsetsBytes = outputOffsetsTensor.dataType().byteSize();

        outputOffsets = ByteBuffer.allocateDirect(ouputOffsetsBytes * outputOffsetsTensor.numElements());
        outputOffsets.order(ByteOrder.nativeOrder());
    }

    public RigidPoseResult predict(FritzVisionImage visionImage) {
        return null;
    }

    /**
     * Identify keypoints in a rigid pose.
     *
     * @return RigidPoseResult
     * @hide
     */
    public RigidPoseResult predict(FritzCVImage visionCVImage) {
        Mat mat = visionCVImage.rotate();
        Mat imgForInput = new Mat();
        Imgproc.resize(mat, imgForInput, inputCVSize);
        imgForInput.get(0, 0, inputData);
        ByteImage byteImage = new ByteImage(inputData, imgForInput.width(), imgForInput.height());
        preprocess(byteImage);

        rewindOutputs();

        Object[] inputArray = {inputByteBuffer};
        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, outputHeatmaps);
        outputMap.put(1, outputOffsets);
        interpreter.runForMultipleInputsOutputs(inputArray, outputMap);

        int outputWidth = outputSize.getWidth();
        int outputHeight = outputSize.getHeight();

        int inputWidth = inputSize.getWidth();
        int inputHeight = inputSize.getHeight();


        HeatmapScores heatmapScores = new HeatmapScores(outputHeatmaps, outputHeight, outputWidth, numKeypoints);
        Offsets offsets = new Offsets(outputOffsets, outputHeight, outputWidth, numKeypoints);

        float[] maxScoresForParts = new float[numKeypoints];
        int[] maxRowIndex = new int[numKeypoints];
        int[] maxColIndex = new int[numKeypoints];

        for (int partId = 0; partId < numKeypoints; partId++) {
            for (int row = 0; row < outputHeight; row++) {
                for (int col = 0; col < outputWidth; col++) {
                    float score = heatmapScores.getScore(partId, col, row);
                    if (score > maxScoresForParts[partId]) {
                        maxScoresForParts[partId] = score;
                        maxRowIndex[partId] = row;
                        maxColIndex[partId] = col;
                    }
                }
            }
        }

        int numOverThreshold = 0;
        for (int partId = 0; partId < numKeypoints; partId++) {
            if (maxScoresForParts[partId] >= this.scoreThreshold) {
                numOverThreshold++;
            }
        }

        if (numOverThreshold < this.minPartsOverThreshold) {
            return null;
        }

        Point[] partLocationOnImage = new Point[numKeypoints];
        for (int partId = 0; partId < numKeypoints; partId++) {
            int row = maxRowIndex[partId];
            int col = maxColIndex[partId];
            PointF offsetPoint = offsets.getOffsetPoint(partId, col, row);

            float scaleX = (float) inputWidth / outputWidth;
            float scaleY = (float) inputHeight / outputHeight;

            float xLoc = col * scaleX + offsetPoint.x;
            float yLoc = row * scaleY + offsetPoint.y;

            partLocationOnImage[partId] = new Point(xLoc, yLoc);
        }

        RigidPoseResult poseResult = new RigidPoseResult(partLocationOnImage, maxScoresForParts, inputSize);
        return poseResult;
    }

    private void rewindOutputs() {
        outputHeatmaps.rewind();
        outputOffsets.rewind();
    }

    private void preprocess(ByteImage byteImage) {
        inputByteBuffer.rewind();
        byte[] buffer = byteImage.getCopyOfImageData();

        for (int i = 0; i < buffer.length; i += NUM_CHANNELS) {

            // In this case for Mat byte arrays, the lower bytes are red.
            float rByte = (float) (buffer[i] & 0xFF);
            float gByte = (float) (buffer[i + 1] & 0xFF);
            float bByte = (float) (buffer[i + 2] & 0xFF);

            inputByteBuffer.putFloat(rByte / 255 - .5f);
            inputByteBuffer.putFloat(gByte / 255 - .5f);
            inputByteBuffer.putFloat(bByte / 255 - .5f);
        }
    }
}
