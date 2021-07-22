package ai.fritz.sdktests.rigidpose;

import android.graphics.Bitmap;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import ai.fritz.sdktests.BaseFritzTest;
import ai.fritz.sdktests.TestingAsset;
import ai.fritz.sdktests.TestingAssetHelper;
import ai.fritz.visionCV.FritzCVImage;
import ai.fritz.visionCV.FritzVisionCV;
import ai.fritz.visionCV.rigidpose.FritzVisionRigidPosePredictor;
import ai.fritz.visionCV.rigidpose.FritzVisionRigidPosePredictorOptions;
import ai.fritz.visionCV.rigidpose.RigidPoseResult;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
@Ignore
public class RigidPoseTest extends BaseFritzTest {
    @Test
    public void testRigidPoseModel() {
        Bitmap testImage = TestingAssetHelper.getBitmapForAsset(appContext, TestingAsset.DENTASTIX);
        FritzCVImage cvImage = FritzCVImage.fromBitmap(testImage);
        FritzVisionRigidPosePredictorOptions options = new FritzVisionRigidPosePredictorOptions();
        FritzVisionRigidPosePredictor predictor = FritzVisionCV.RigidPose.getPredictor(new DentastixPortraitOnDeviceModel(), options);
        RigidPoseResult poseResult = predictor.predict(cvImage);
        assertNotNull(poseResult);
    }
}
