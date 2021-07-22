package ai.fritz.sdktests;

import android.util.Log;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tensorflow.lite.Interpreter;

import java.util.ArrayList;
import java.util.List;

import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.ModelVariant;
import ai.fritz.vision.base.FritzVisionPredictor;
import ai.fritz.vision.imagelabeling.FritzVisionLabelPredictor;
import ai.fritz.vision.imagelabeling.FritzVisionLabelPredictorOptions;
import ai.fritz.vision.imagelabeling.LabelingOnDeviceModel;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictor;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictorOptions;
import ai.fritz.vision.imagesegmentation.SegmentationOnDeviceModel;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictor;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictorOptions;
import ai.fritz.vision.objectdetection.ObjectDetectionOnDeviceModel;
import ai.fritz.vision.poseestimation.FritzVisionPosePredictor;
import ai.fritz.vision.poseestimation.FritzVisionPosePredictorOptions;
import ai.fritz.vision.poseestimation.PoseOnDeviceModel;
import ai.fritz.vision.styletransfer.FritzVisionStylePredictor;
import ai.fritz.vision.styletransfer.FritzVisionStylePredictorOptions;

@RunWith(AndroidJUnit4.class)
public class PerformanceTests extends BaseFritzTest {
    private static final String TAG = PerformanceTests.class.getSimpleName();
    private static final int MAX_THREADS = 16;
    private static final int NUM_PREDICTIONS = 50;
    private static final int WARM_UP_PREDICTIONS = 5;

    @Test
    @Ignore
    public void testRunAllBenchmarks() {
        runPoseBenchmarks();
        runStyleBenchmarks();
        runImageLabelingBenchmarks();
        runObjectDetectionBenchmarks();
        runImageSegmentationBenchmarks();
    }

    private void runStyleBenchmarks() {
        printTitle("STYLE TRANSFER");
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.FAMILY);
        FritzVisionStylePredictorOptions gpuOptions = new FritzVisionStylePredictorOptions();
        FritzOnDeviceModel styleModel = FritzVisionModels.getPaintingStyleModels().getStarryNight();
        FritzVisionStylePredictor gpuPredictor = FritzVision.StyleTransfer.getPredictor(styleModel, gpuOptions);
        Interpreter tflInterpreter = gpuPredictor.getInterpreter();
        runPredictionTimings("GPU Enabled", testImage, tflInterpreter, gpuPredictor);
        gpuPredictor.close();

        // Use NNAPI
        FritzVisionStylePredictorOptions nnapiOptions = new FritzVisionStylePredictorOptions();
        nnapiOptions.useNNAPI = true;
        FritzVisionStylePredictor nnapiPredictor = FritzVision.StyleTransfer.getPredictor(styleModel, nnapiOptions);
        runPredictionTimings("NNAPI enabled", testImage, nnapiPredictor.getInterpreter(), nnapiPredictor);
        nnapiPredictor.close();


        // Use threads
        FritzVisionStylePredictorOptions numThreadOptions = new FritzVisionStylePredictorOptions();
        for (int numThreads = 1; numThreads <= MAX_THREADS; numThreads = numThreads * 2) {
            numThreadOptions.numThreads = numThreads;
            FritzVisionStylePredictor threadPredictor = FritzVision.StyleTransfer.getPredictor(styleModel, numThreadOptions);
            runPredictionTimings("Using " + numThreads + " threads", testImage, threadPredictor.getInterpreter(), threadPredictor);
            threadPredictor.close();
        }
    }

    private void runObjectDetectionBenchmarks() {
        printTitle("OBJECT DETECTION");
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.FAMILY);
        FritzVisionObjectPredictorOptions gpuOptions = new FritzVisionObjectPredictorOptions();
        ObjectDetectionOnDeviceModel objectModel = FritzVisionModels.getObjectDetectionOnDeviceModel();
        FritzVisionObjectPredictor gpuPredictor = FritzVision.ObjectDetection.getPredictor(objectModel, gpuOptions);
        Interpreter tflInterpreter = gpuPredictor.getInterpreter();
        runPredictionTimings("GPU Enabled", testImage, tflInterpreter, gpuPredictor);
        gpuPredictor.close();


        // Use NNAPI
        FritzVisionObjectPredictorOptions nnapiOptions = new FritzVisionObjectPredictorOptions();
        nnapiOptions.useNNAPI = true;
        FritzVisionObjectPredictor nnapiPredictor = FritzVision.ObjectDetection.getPredictor(objectModel, nnapiOptions);
        runPredictionTimings("NNAPI enabled", testImage, nnapiPredictor.getInterpreter(), nnapiPredictor);
        nnapiPredictor.close();


        // Use threads
        FritzVisionObjectPredictorOptions numThreadOptions = new FritzVisionObjectPredictorOptions();
        for (int numThreads = 1; numThreads <= MAX_THREADS; numThreads = numThreads * 2) {
            numThreadOptions.numThreads = numThreads;
            FritzVisionObjectPredictor threadPredictor = FritzVision.ObjectDetection.getPredictor(objectModel, numThreadOptions);
            runPredictionTimings("Using " + numThreads + " threads", testImage, threadPredictor.getInterpreter(), threadPredictor);
            threadPredictor.close();
        }
    }

    private void runImageLabelingBenchmarks() {
        printTitle("IMAGE LABELING");
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.FAMILY);
        LabelingOnDeviceModel labelingModel = FritzVisionModels.getImageLabelingOnDeviceModel();

        FritzVisionLabelPredictorOptions gpuOptions = new FritzVisionLabelPredictorOptions();
        FritzVisionLabelPredictor gpuPredictor = FritzVision.ImageLabeling.getPredictor(labelingModel, gpuOptions);
        Interpreter tflInterpreter = gpuPredictor.getInterpreter();
        runPredictionTimings("GPU Enabled", testImage, tflInterpreter, gpuPredictor);
        gpuPredictor.close();


        // Use NNAPI
        FritzVisionLabelPredictorOptions nnapiOptions = new FritzVisionLabelPredictorOptions();
        nnapiOptions.useNNAPI = true;
        FritzVisionLabelPredictor nnapiPredictor = FritzVision.ImageLabeling.getPredictor(labelingModel, nnapiOptions);
        runPredictionTimings("NNAPI enabled", testImage, nnapiPredictor.getInterpreter(), nnapiPredictor);
        nnapiPredictor.close();


        // Use threads
        FritzVisionLabelPredictorOptions numThreadOptions = new FritzVisionLabelPredictorOptions();
        for (int numThreads = 1; numThreads <= MAX_THREADS; numThreads = numThreads * 2) {
            numThreadOptions.numThreads = numThreads;
            FritzVisionLabelPredictor threadPredictor = FritzVision.ImageLabeling.getPredictor(labelingModel, numThreadOptions);
            runPredictionTimings("Using " + numThreads + " threads", testImage, threadPredictor.getInterpreter(), threadPredictor);
            threadPredictor.close();
        }
    }

    private void runPoseBenchmarks() {
        printTitle("POSE ESTIMATION");
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.FAMILY);
        FritzVisionPosePredictorOptions gpuOptions = new FritzVisionPosePredictorOptions();
        PoseOnDeviceModel poseModel = FritzVisionModels.getHumanPoseEstimationOnDeviceModel(ModelVariant.FAST);
        FritzVisionPosePredictor gpuPredictor = FritzVision.PoseEstimation.getPredictor(poseModel, gpuOptions);
        Interpreter tflInterpreter = gpuPredictor.getInterpreter();
        runPredictionTimings("GPU Enabled", testImage, tflInterpreter, gpuPredictor);
        gpuPredictor.close();


        // Use NNAPI
        FritzVisionPosePredictorOptions nnapiOptions = new FritzVisionPosePredictorOptions();
        nnapiOptions.useNNAPI = true;
        FritzVisionPosePredictor nnapiPredictor = FritzVision.PoseEstimation.getPredictor(poseModel, nnapiOptions);
        runPredictionTimings("NNAPI enabled", testImage, nnapiPredictor.getInterpreter(), nnapiPredictor);
        nnapiPredictor.close();


        // Use threads
        FritzVisionPosePredictorOptions numThreadOptions = new FritzVisionPosePredictorOptions();
        for (int numThreads = 1; numThreads <= MAX_THREADS; numThreads = numThreads * 2) {
            numThreadOptions.numThreads = numThreads;
            FritzVisionPosePredictor threadPredictor = FritzVision.PoseEstimation.getPredictor(poseModel, numThreadOptions);
            runPredictionTimings("Using " + numThreads + " threads", testImage, threadPredictor.getInterpreter(), threadPredictor);
            threadPredictor.close();
        }
    }

    private void runImageSegmentationBenchmarks() {
        printTitle("IMAGE SEGMENTATION");
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.GIRL);

        List<SegmentationOnDeviceModel> modelsToTest = new ArrayList<>();
        modelsToTest.add(FritzVisionModels.getSkySegmentationOnDeviceModel(ModelVariant.FAST));
        modelsToTest.add(FritzVisionModels.getHairSegmentationOnDeviceModel(ModelVariant.FAST));
        modelsToTest.add(FritzVisionModels.getPeopleSegmentationOnDeviceModel(ModelVariant.FAST));

        String[] modelTitles = {"Sky", "Hair", "People", "People Medium"};
        int titleIndex = 0;
        for (SegmentationOnDeviceModel onDeviceModel : modelsToTest) {
            Log.d(TAG, modelTitles[titleIndex++]);
            // Using the GPU
            FritzVisionSegmentationPredictorOptions gpuOptions = new FritzVisionSegmentationPredictorOptions();
            FritzVisionSegmentationPredictor gpuPredictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel, gpuOptions);
            runPredictionTimings("GPU Enabled", testImage, gpuPredictor.getInterpreter(), gpuPredictor);
            gpuPredictor.close();


            // Use NNAPI
            FritzVisionSegmentationPredictorOptions nnapiOptions = new FritzVisionSegmentationPredictorOptions();
            nnapiOptions.useNNAPI = true;
            FritzVisionSegmentationPredictor nnapiPredictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel, nnapiOptions);
            runPredictionTimings("NNAPI enabled", testImage, nnapiPredictor.getInterpreter(), nnapiPredictor);
            nnapiPredictor.close();


            // Use threads
            FritzVisionSegmentationPredictorOptions numThreadOptions = new FritzVisionSegmentationPredictorOptions();
            for (int numThreads = 1; numThreads <= MAX_THREADS; numThreads = numThreads * 2) {
                numThreadOptions.numThreads = numThreads;
                FritzVisionSegmentationPredictor threadPredictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel, numThreadOptions);
                runPredictionTimings("Using " + numThreads + " threads", testImage, threadPredictor.getInterpreter(), threadPredictor);
                threadPredictor.close();
            }
        }
    }

    private void printTitle(String title) {
        Log.d(TAG, "---------------------------------");
        Log.d(TAG, title);
        Log.d(TAG, "---------------------------------");
    }

    private void runPredictionTimings(String title, FritzVisionImage testImage, Interpreter interpreter, FritzVisionPredictor predictor) {
        printTitle(title);

        // Add a couple of warm up predictions
        for (int i = 0; i < WARM_UP_PREDICTIONS; i++) {
            predictor.predict(testImage);
        }

        long maxTime = Long.MIN_VALUE, maxPredictionTime = Long.MIN_VALUE;
        long minTime = Long.MAX_VALUE, minPredictionTime = Long.MAX_VALUE;
        long total = 0, totalPrediction = 0;
        for (int i = 0; i < NUM_PREDICTIONS; i++) {
            long start = System.currentTimeMillis();
            predictor.predict(testImage);
            long predictionTime = System.currentTimeMillis() - start;
            totalPrediction += predictionTime;
            maxPredictionTime = Math.max(maxPredictionTime, predictionTime);
            minPredictionTime = Math.min(minPredictionTime, predictionTime);

            Long inferenceTime = interpreter.getLastNativeInferenceDurationNanoseconds();
            if (inferenceTime != null) {
                maxTime = Math.max(maxTime, inferenceTime.longValue());
                minTime = Math.min(minTime, inferenceTime.longValue());
                total += inferenceTime.longValue();
            }


        }

        predictor.close();

        Log.d(TAG, "Avg Inference Time: " + (total / NUM_PREDICTIONS / 1e6) + "ms");
        Log.d(TAG, "Max Inference Time: " + (maxTime / 1e6) + "ms");
        Log.d(TAG, "Min Inference Time: " + (minTime / 1e6) + "ms");

        Log.d(TAG, "Avg Prediction Time: " + (totalPrediction / NUM_PREDICTIONS) + "ms");
        Log.d(TAG, "Max Prediction Time: " + maxPredictionTime + "ms");
        Log.d(TAG, "Min Prediction Time: " + minPredictionTime + "ms");
    }
}