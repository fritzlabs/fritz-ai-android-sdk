package ai.fritz.vision.poseestimation;

import android.util.Size;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.fritz.core.OutputTensor;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.ImageInputTensor;
import ai.fritz.vision.base.FritzVisionRecordablePredictor;
import ai.fritz.vision.filter.PoseSmoother;

/**
 * The predictor class for pose detection.
 */
public class FritzVisionPosePredictor extends FritzVisionRecordablePredictor {

    private static final String TAG = FritzVisionPosePredictor.class.getSimpleName();

    private static final int LOCAL_MAX_RADIUS = 1;

    private Size outputGridSize;

    private ImageInputTensor inputTensor = new ImageInputTensor("Image Input Tensor", 0);
    private OutputTensor outputHeatmaps = new OutputTensor("Heatmap", 0);
    private OutputTensor outputOffsets = new OutputTensor("Offsets", 1);
    private OutputTensor outputDisplacementsFwd = new OutputTensor("DisplacementsFwd", 2);
    private OutputTensor outputDisplacementsBwd = new OutputTensor("DisplacementsBwd", 3);

    // Model inference configs
    Skeleton skeleton;
    boolean usesDisplacements;
    int outputStride;

    private FritzVisionPosePredictorOptions options;
    private PoseSmoother poseSmoother;

    public FritzVisionPosePredictor(PoseOnDeviceModel onDeviceModel, FritzVisionPosePredictorOptions options) {
        super(onDeviceModel, options);
        this.options = options;

        skeleton = onDeviceModel.getSkeleton();
        usesDisplacements = onDeviceModel.useDisplacements();
        outputStride = onDeviceModel.getOutputStride();

        inputTensor.setupInputBuffer(interpreter);
        inputSize = inputTensor.getImageDimensions();

        outputHeatmaps.setupOutputBuffer(interpreter);
        outputOffsets.setupOutputBuffer(interpreter);
        outputGridSize = outputHeatmaps.getBounds();

        if (usesDisplacements) {
            outputDisplacementsFwd.setupOutputBuffer(interpreter);
            outputDisplacementsBwd.setupOutputBuffer(interpreter);
        }

        initializePoseSmoother();
    }

    private void initializePoseSmoother() {
        if (options.smoothingOptions != null) {
            poseSmoother = options.smoothingOptions.buildPoseSmoother(skeleton.getNumKeypoints());
        }
    }

    /**
     * Identify and create pixel-level masks for all items in visionImage.
     *
     * @param visionImage The image to run inference on.
     * @return {@link FritzVisionPoseResult}
     */
    public FritzVisionPoseResult predict(FritzVisionImage visionImage) {
        inputTensor.preprocess(visionImage, DEFAULT_PREPROCESSING_PARAMS);
        rewindOutputs();

        Object[] inputArray = {inputTensor.buffer};
        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(outputHeatmaps.getTensorIndex(), outputHeatmaps.buffer);
        outputMap.put(outputOffsets.getTensorIndex(), outputOffsets.buffer);

        if (usesDisplacements) {
            outputMap.put(outputDisplacementsFwd.getTensorIndex(), outputDisplacementsFwd.buffer);
            outputMap.put(outputDisplacementsBwd.getTensorIndex(), outputDisplacementsBwd.buffer);
        }
        interpreter.runForMultipleInputsOutputs(inputArray, outputMap);

        HeatmapScores heatmapScores = new HeatmapScores(outputHeatmaps.buffer, outputGridSize.getHeight(), outputGridSize.getWidth(), skeleton.getNumKeypoints());
        Offsets offsets = new Offsets(outputOffsets.buffer, outputGridSize.getHeight(), outputGridSize.getWidth(), skeleton.getNumKeypoints());

        List<Pose> poses = new ArrayList<>();
        if (usesDisplacements) {
            Displacements displacementFwd = new Displacements(outputDisplacementsFwd.buffer, outputGridSize.getHeight(), outputGridSize.getWidth(), skeleton.getNumEdges());
            Displacements displacementBwd = new Displacements(outputDisplacementsBwd.buffer, outputGridSize.getHeight(), outputGridSize.getWidth(), skeleton.getNumEdges());
            PoseDecoderWithDisplacements poseDecoder = new PoseDecoderWithDisplacements(heatmapScores, offsets, displacementFwd, displacementBwd, inputSize, skeleton);
            int maxPoses = options.maxPosesToDetect;
            poses = poseDecoder.decodeMultiplePoses(outputStride, maxPoses, options.minPartThreshold, options.nmsRadius, LOCAL_MAX_RADIUS);
        } else {
            PoseDecoder poseDecoder = new PoseDecoder(heatmapScores, offsets, outputGridSize, inputSize, options.minPartThreshold, skeleton);
            Pose pose = poseDecoder.decodePose();
            if (pose != null) {
                poses.add(pose);
            }
        }

        // TODO: Make pose smoothing work for multiple poses detected. Need to identify each instance.
        if (poseSmoother != null && options.maxPosesToDetect == 1) {
            poses = smoothPoses(poses);
        }

        FritzVisionPoseResult poseResult = new FritzVisionPoseResult(poses, options.minPoseThreshold, inputSize, visionImage.encodedSize());
        return poseResult;
    }

    private List<Pose> smoothPoses(List<Pose> poses) {
        List<Pose> smoothedPoses = new ArrayList<>();
        for (Pose pose : poses) {
            smoothedPoses.add(poseSmoother.smooth(pose));
        }

        return smoothedPoses;
    }

    private void rewindOutputs() {
        outputHeatmaps.rewind();
        outputOffsets.rewind();
        if (usesDisplacements) {
            outputDisplacementsFwd.rewind();
            outputDisplacementsBwd.rewind();
        }
    }
}
