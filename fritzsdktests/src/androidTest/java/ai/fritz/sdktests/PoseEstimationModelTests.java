package ai.fritz.sdktests;

import android.graphics.Bitmap;
import android.graphics.PointF;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import ai.fritz.sdktests.poseestimation.CustomSkeleton;
import ai.fritz.sdktests.poseestimation.CustomSkeletonWithoutPoseChain;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.ModelVariant;
import ai.fritz.vision.filter.OneEuroFilterMethod;
import ai.fritz.vision.poseestimation.FritzVisionPosePredictor;
import ai.fritz.vision.poseestimation.FritzVisionPosePredictorOptions;
import ai.fritz.vision.poseestimation.FritzVisionPoseResult;
import ai.fritz.vision.poseestimation.Keypoint;
import ai.fritz.vision.poseestimation.Pose;
import ai.fritz.vision.poseestimation.PoseOnDeviceModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test prediction on all pose estimation models.
 */
@RunWith(AndroidJUnit4.class)
public class PoseEstimationModelTests extends BaseFritzTest {

    private static final String TAG = PoseEstimationModelTests.class.getSimpleName();

    @Test
    public void testCustomHandPose() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.HAND);

        PoseOnDeviceModel poseEstimationOnDeviceModel = new PoseOnDeviceModel(
                "file:///android_asset/hands.tflite",
                "42457de9f0814f95a9f8bf21376fecb3",
                1,
                new CustomSkeleton(),
                4,
                false
        );
        FritzVisionPosePredictorOptions options = new FritzVisionPosePredictorOptions();
        options.minPartThreshold = 0.1f;
        options.minPoseThreshold = 0.1f;
        FritzVisionPosePredictor predictor = FritzVision.PoseEstimation.getPredictor(poseEstimationOnDeviceModel, options);
        FritzVisionPoseResult poseResult = predictor.predict(testImage);
        assertEquals(poseResult.getPoses().size(), 1);
        List<Pose> poses = poseResult.getPoses();
        // Make sure that all but one of the keypoints is on the left half of the image.
        // Get the first pose
        Pose pose = poseResult.getPoses().get(0);
        Keypoint[] keypoints = pose.getKeypoints();
        int countOnRight = 0;
        for (int idx = 0; idx < keypoints.length; idx++) {
            PointF keypointPoisition = keypoints[idx].getPosition();
            // the model's input size is 200 x 260
            if (keypointPoisition.x > 200.0 / 2.0) {
                countOnRight++;
            }
        }
        assertEquals(countOnRight, 1);
        Bitmap result = testImage.overlaySkeletons(poses);
        assertNotNull(result);

        predictor.close();
    }

    @Test
    public void testCustomHandPoseSkeletonWithoutPoseChain() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.HAND);

        CustomSkeletonWithoutPoseChain skeleton = new CustomSkeletonWithoutPoseChain();
        PoseOnDeviceModel poseEstimationOnDeviceModel = new PoseOnDeviceModel(
                "file:///android_asset/hands.tflite",
                "42457de9f0814f95a9f8bf21376fecb3",
                1,
                skeleton,
                4,
                false
        );
        FritzVisionPosePredictorOptions options = new FritzVisionPosePredictorOptions();
        options.minPartThreshold = 0.1f;
        options.minPoseThreshold = 0.1f;
        FritzVisionPosePredictor predictor = FritzVision.PoseEstimation.getPredictor(poseEstimationOnDeviceModel, options);
        FritzVisionPoseResult poseResult = predictor.predict(testImage);
        assertEquals(poseResult.getPoses().size(), 1);
        List<Pose> poses = poseResult.getPoses();
        // Make sure that all but one of the keypoints is on the left half of the image.
        // Get the first pose
        Pose pose = poseResult.getPoses().get(0);
        Keypoint[] keypoints = pose.getKeypoints();
        int countOnRight = 0;
        for (int idx = 0; idx < keypoints.length; idx++) {
            PointF keypointPoisition = keypoints[idx].getPosition();
            // the model's input size is 200 x 260
            if (keypointPoisition.x > 200.0 / 2.0) {
                countOnRight++;
            }
        }
        assertEquals(countOnRight, 1);
        Bitmap result = testImage.overlaySkeletons(poses);
        assertNotNull(result);
        assertTrue(skeleton.getPoseChain().length > 0);
        predictor.close();
    }

    @Test
    public void testPoseEstimationModel() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.FAMILY);
        FritzVisionPosePredictorOptions options = new FritzVisionPosePredictorOptions();

        for(ModelVariant variant: ModelVariant.values()) {
            PoseOnDeviceModel onDeviceModel = FritzVisionModels.getHumanPoseEstimationOnDeviceModel(variant);
            FritzVisionPosePredictor predictor = FritzVision.PoseEstimation.getPredictor(onDeviceModel, options);
            FritzVisionPoseResult poseResult = predictor.predict(testImage);
            assertNotNull(poseResult);

            List<Pose> poses = poseResult.getPoses();
            Bitmap result = testImage.overlaySkeletons(poses);
            assertNotNull(result);

            // At least one pose detected
            assertEquals(poseResult.getPoses().size(), 1);
            predictor.close();
        }
    }

    @Test
    public void testMultiPoseEstimationModelOptions() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.FAMILY);
        FritzVisionPosePredictorOptions options = new FritzVisionPosePredictorOptions();
        options.maxPosesToDetect = 4;
        options.numThreads = 8;

        PoseOnDeviceModel onDeviceModel = FritzVisionModels.getHumanPoseEstimationOnDeviceModel(ModelVariant.ACCURATE);
        FritzVisionPosePredictor predictor = FritzVision.PoseEstimation.getPredictor(onDeviceModel, options);
        FritzVisionPoseResult poseResult = predictor.predict(testImage);
        assertNotNull(poseResult);

        List<Pose> poses = poseResult.getPoses();
        Bitmap result = testImage.overlaySkeletons(poses);
        assertNotNull(result);

        int numPoses = poseResult.getPoses().size();
        assertTrue(numPoses == 3|| numPoses == 4);
        predictor.close();
    }

    @Test
    public void testMultiPoseEstimationModelMirrored() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.FAMILY, true);
        FritzVisionPosePredictorOptions options = new FritzVisionPosePredictorOptions();
        options.maxPosesToDetect = 4;
        options.numThreads = 8;

        PoseOnDeviceModel onDeviceModel = FritzVisionModels.getHumanPoseEstimationOnDeviceModel(ModelVariant.ACCURATE);
        FritzVisionPosePredictor predictor = FritzVision.PoseEstimation.getPredictor(onDeviceModel, options);
        FritzVisionPoseResult poseResult = predictor.predict(testImage);
        assertNotNull(poseResult);

        List<Pose> poses = poseResult.getPoses();
        Bitmap result = testImage.overlaySkeletons(poses);
        assertNotNull(result);

        assertEquals(poseResult.getPoses().size(), 2);
        predictor.close();
    }

    @Test
    public void testSmoothing() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.FAMILY);
        FritzVisionPosePredictorOptions options = new FritzVisionPosePredictorOptions();
        options.smoothingOptions = new OneEuroFilterMethod();

        PoseOnDeviceModel onDeviceModel = FritzVisionModels.getHumanPoseEstimationOnDeviceModel(ModelVariant.FAST);
        FritzVisionPosePredictor predictor = FritzVision.PoseEstimation.getPredictor(onDeviceModel, options);
        FritzVisionPoseResult poseResult = predictor.predict(testImage);
        assertNotNull(poseResult);
    }
}
