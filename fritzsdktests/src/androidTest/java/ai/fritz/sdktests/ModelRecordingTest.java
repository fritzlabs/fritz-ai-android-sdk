package ai.fritz.sdktests;

import android.util.Log;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ai.fritz.core.annotations.DataAnnotation;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.base.FritzVisionRecordablePredictor;
import ai.fritz.vision.imagelabeling.FritzVisionLabelPredictor;
import ai.fritz.vision.imagelabeling.FritzVisionLabelResult;
import ai.fritz.vision.imagelabeling.LabelingOnDeviceModel;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictor;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationResult;
import ai.fritz.vision.imagesegmentation.SegmentationClasses;
import ai.fritz.vision.imagesegmentation.SegmentationOnDeviceModel;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictor;
import ai.fritz.vision.objectdetection.FritzVisionObjectResult;
import ai.fritz.vision.objectdetection.ObjectDetectionOnDeviceModel;
import ai.fritz.vision.poseestimation.FritzVisionPosePredictor;
import ai.fritz.vision.poseestimation.FritzVisionPosePredictorOptions;
import ai.fritz.vision.poseestimation.FritzVisionPoseResult;
import ai.fritz.vision.poseestimation.HumanSkeleton;
import ai.fritz.vision.poseestimation.PoseOnDeviceModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * After we switch to using staging for testing, remove this ignore.
 */
@Ignore
public class ModelRecordingTest extends BaseFritzTest {

    @Test
    public void testRecordDetectedAnnotations() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.CAT);

        ObjectDetectionOnDeviceModel onDeviceModel = ObjectDetectionOnDeviceModel.buildFromModelConfigFile("object_detection_model.json");
        FritzVisionObjectPredictor predictor = FritzVision.ObjectDetection.getPredictor(onDeviceModel);
        FritzVisionObjectResult objectResult = predictor.predict(testImage);
        List<DataAnnotation> predictedAnnotations = objectResult.toAnnotations();
        record(predictor, testImage, predictedAnnotations, null);
    }

    @Test
    public void testRecordPoseAnnotation() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.FAMILY);
        PoseOnDeviceModel onDeviceModel = PoseOnDeviceModel.buildFromModelConfigFile("pose_recording_model.json", new HumanSkeleton());
        FritzVisionPosePredictorOptions options = new FritzVisionPosePredictorOptions();
        options.maxPosesToDetect = 4;

        FritzVisionPosePredictor predictor = FritzVision.PoseEstimation.getPredictor(onDeviceModel, options);
        FritzVisionPoseResult poseResult = predictor.predict(testImage);
        List<DataAnnotation> predictedAnnotations = poseResult.toAnnotations();
        record(predictor, testImage, predictedAnnotations, null);
    }

    @Test
    public void testRecordLabelingAnnotation() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.CAT);
        LabelingOnDeviceModel onDeviceModel = LabelingOnDeviceModel.buildFromModelConfigFile("label_recording_model.json");

        FritzVisionLabelPredictor predictor = FritzVision.ImageLabeling.getPredictor(onDeviceModel);
        FritzVisionLabelResult labelResult = predictor.predict(testImage);
        List<DataAnnotation> predictedAnnotations = labelResult.toAnnotations();
        record(predictor, testImage, predictedAnnotations, null);
    }

    @Test
    public void testRecordSegmentationAnnotation() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.FAMILY);
        SegmentationOnDeviceModel onDeviceModel = SegmentationOnDeviceModel.buildFromModelConfigFile("segmentation_recording_model.json", SegmentationClasses.PEOPLE);

        FritzVisionSegmentationPredictor predictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel);
        FritzVisionSegmentationResult segmentationResult = predictor.predict(testImage);
        List<DataAnnotation> predictedAnnotations = segmentationResult.toAnnotations();
        assertEquals(predictedAnnotations.size(), 1);
        record(predictor, testImage, predictedAnnotations, null);

        // test filtering out annotations by area
        predictedAnnotations = segmentationResult.toAnnotations(0.5f, 1.0f);
        assertEquals(predictedAnnotations.size(), 0);
    }

    private void record(FritzVisionRecordablePredictor predictor, FritzVisionImage visionImage, List<DataAnnotation> predictedAnnotations, List<DataAnnotation> userModifiedAnnotations) {
        final CountDownLatch latch = new CountDownLatch(1);
        predictor.record(visionImage, predictedAnnotations, userModifiedAnnotations, () -> {
            Log.d("SUCCESS", "success");
            latch.countDown();
            return null;
        }, () -> {
            Log.d("FAIL", "fail");
            fail();
            return null;
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted.");
        }
    }
}
