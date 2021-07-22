package ai.fritz.sdktests;

import android.graphics.Bitmap;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ai.fritz.sdktests.BaseFritzTest;
import ai.fritz.sdktests.TestingAsset;
import ai.fritz.sdktests.TestingAssetHelper;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.FritzVisionObject;
import ai.fritz.vision.PredictorStatusListener;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictor;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictorOptions;
import ai.fritz.vision.objectdetection.FritzVisionObjectResult;
import ai.fritz.vision.objectdetection.ObjectDetectionManagedModel;
import ai.fritz.vision.objectdetection.ObjectDetectionOnDeviceModel;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test prediction on all object detection models.
 */
@RunWith(AndroidJUnit4.class)
public class ObjectDetectionModelTests extends BaseFritzTest {

    public static final String CUSTOM_MODEL_PATH = "file:///android_asset/soccer_balls.tflite";
    public static final String CUSTOM_MODEL_ID = "abc";
    public static final List<String> CUSTOM_LABELS = Arrays.asList("???", "soccer ball");
    public static final int CUSTOM_MODEL_VERSION = 1;

    @Test
    public void testOverlayDetection() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.FAMILY);
        ObjectDetectionOnDeviceModel onDeviceModel = FritzVisionModels.getObjectDetectionOnDeviceModel();

        FritzVisionObjectPredictor predictor = FritzVision.ObjectDetection.getPredictor(onDeviceModel);
        FritzVisionObjectResult objectResult = predictor.predict(testImage);
        assertNotNull(objectResult);

        List<FritzVisionObject> catObjects = objectResult.getVisionObjectsByClass("person");
        Bitmap result = testImage.overlayBoundingBoxes(catObjects);
        assertNotNull(result);
        predictor.close();
    }

    @Test
    public void testOverlayDetections() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.CAT);
        ObjectDetectionOnDeviceModel onDeviceModel = FritzVisionModels.getObjectDetectionOnDeviceModel();

        FritzVisionObjectPredictor predictor = FritzVision.ObjectDetection.getPredictor(onDeviceModel);
        FritzVisionObjectResult objectResult = predictor.predict(testImage);
        assertNotNull(objectResult);

        Bitmap result = testImage.overlayBoundingBoxes(objectResult.getObjects());
        assertNotNull(result);
        predictor.close();
    }

    @Test
    public void testOverlayDetectionsCenternetModel() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.SOCCER_BALLS);
        ObjectDetectionOnDeviceModel onDeviceModel = new ObjectDetectionOnDeviceModel(
                CUSTOM_MODEL_PATH,
                CUSTOM_MODEL_ID,
                CUSTOM_MODEL_VERSION,
                false,
                CUSTOM_LABELS
        );

        FritzVisionObjectPredictorOptions options = new FritzVisionObjectPredictorOptions();
        options.confidenceThreshold = 0.01f;

        FritzVisionObjectPredictor predictor = FritzVision.ObjectDetection.getPredictor(onDeviceModel, options);
        FritzVisionObjectResult objectResult = predictor.predict(testImage);
        assertNotNull(objectResult);

        Bitmap result = testImage.overlayBoundingBoxes(objectResult.getObjects());
        assertNotNull(result);
        predictor.close();
    }

    @Test
    public void testObjectDetectionModel() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.CAT);

        ObjectDetectionOnDeviceModel onDeviceModel = FritzVisionModels.getObjectDetectionOnDeviceModel();
        FritzVisionObjectPredictor predictor = FritzVision.ObjectDetection.getPredictor(onDeviceModel);
        FritzVisionObjectResult objectResult = predictor.predict(testImage);
        assertNotNull(objectResult);
        List<FritzVisionObject> visionObjectList = objectResult.getObjects();

        boolean containsCat = false;
        for (FritzVisionObject visionObject : visionObjectList) {
            if (visionObject.getVisionLabel().getText().equalsIgnoreCase("cat")) {
                containsCat = true;
                break;
            }
        }

        assertTrue(containsCat);
        predictor.close();
    }

    @Test
    public void testLoadObjectDetectionModelOTA() {
        ObjectDetectionManagedModel managedModel = FritzVisionModels.getObjectDetectionManagedModel();
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.CAT);

        final CountDownLatch latch = new CountDownLatch(1);

        FritzVision.ObjectDetection.loadPredictor(managedModel, new PredictorStatusListener<FritzVisionObjectPredictor>() {
            @Override
            public void onPredictorReady(FritzVisionObjectPredictor predictor) {
                FritzVisionObjectResult objectResult = predictor.predict(testImage);
                List<FritzVisionObject> visionObjects = objectResult.getObjects();

                boolean containsCat = false;
                for (FritzVisionObject visionObject : visionObjects) {
                    if (visionObject.getVisionLabel().getText().equalsIgnoreCase("cat")) {
                        containsCat = true;
                        break;
                    }
                }

                assertTrue(containsCat);
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail();
        }
    }
}
