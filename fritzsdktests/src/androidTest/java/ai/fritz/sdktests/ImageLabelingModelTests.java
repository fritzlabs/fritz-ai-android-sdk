package ai.fritz.sdktests;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ai.fritz.sdktests.validators.LabelResultValidator;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionLabel;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.PredictorStatusListener;
import ai.fritz.vision.imagelabeling.FritzVisionLabelPredictor;
import ai.fritz.vision.imagelabeling.FritzVisionLabelPredictorOptions;
import ai.fritz.vision.imagelabeling.FritzVisionLabelResult;
import ai.fritz.vision.imagelabeling.LabelingManagedModel;
import ai.fritz.vision.imagelabeling.LabelingOnDeviceModel;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for image labeling model
 */
@RunWith(AndroidJUnit4.class)
public class ImageLabelingModelTests extends BaseFritzTest {

//    @Test
//    public void testImageLabelModel() {
//        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.CAT);
//        FritzVisionLabelPredictorOptions options = new FritzVisionLabelPredictorOptions();
//
//        LabelingOnDeviceModel onDeviceModel = FritzVisionModels.getImageLabelingOnDeviceModel();
//        FritzVisionLabelPredictor predictor = FritzVision.ImageLabeling.getPredictor(onDeviceModel, options);
//
//        FritzVisionLabelResult labelResult = predictor.predict(testImage);
//        LabelResultValidator validator = new LabelResultValidator(labelResult);
//        validator.assertLabelExists("cat");
//        predictor.close();
//    }

    @Test
    public void testLabelingModelNonQuantized() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.TIGER);

        LabelingOnDeviceModel onDeviceModel = LabelingOnDeviceModel.buildFromModelConfigFile("labeling_animals.json");
        FritzVisionLabelPredictor predictor = FritzVision.ImageLabeling.getPredictor(onDeviceModel);
        predictor.predict(testImage);
    }

    @Test
    public void testLoadLabelModelOTA() {
        LabelingManagedModel managedModel = FritzVisionModels.getImageLabelingManagedModel();
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.CAT);

        final CountDownLatch latch = new CountDownLatch(1);

        FritzVision.ImageLabeling.loadPredictor(managedModel, new PredictorStatusListener<FritzVisionLabelPredictor>() {
            @Override
            public void onPredictorReady(FritzVisionLabelPredictor predictor) {
                FritzVisionLabelResult labelResult = predictor.predict(testImage);
                LabelResultValidator validator = new LabelResultValidator(labelResult);
                validator.assertLabelExists("cat");
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test
    public void testSetConfidence() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.CAT);

        // This is the new way of setting up options (less verbose)
        FritzVisionLabelPredictorOptions options = new FritzVisionLabelPredictorOptions();
        options.numThreads = 4;
        options.confidenceThreshold = .3f;

        LabelingOnDeviceModel onDeviceModel = FritzVisionModels.getImageLabelingOnDeviceModel();

        FritzVisionLabelPredictor predictor = FritzVision.ImageLabeling.getPredictor(onDeviceModel, options);
        FritzVisionLabelResult labelResult = predictor.predict(testImage);
        LabelResultValidator validator = new LabelResultValidator(labelResult);
        FritzVisionLabel visionLabel = validator.assertLabelExists("cat");

        // Assert that the confidence level is greater than the threshold.
        assertTrue(visionLabel.getConfidence() >= .3f);
    }
}
