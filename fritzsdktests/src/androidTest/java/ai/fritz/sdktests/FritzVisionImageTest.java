package ai.fritz.sdktests;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import ai.fritz.sdktests.validators.ObjectResultValidator;
import ai.fritz.sdktests.validators.PoseResultValidator;
import ai.fritz.vision.ByteImage;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.FritzVisionObject;
import ai.fritz.vision.FritzVisionOrientation;
import ai.fritz.vision.ImageOrientation;
import ai.fritz.vision.ImageRotation;
import ai.fritz.vision.ModelVariant;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictor;
import ai.fritz.vision.objectdetection.FritzVisionObjectResult;
import ai.fritz.vision.objectdetection.ObjectDetectionOnDeviceModel;
import ai.fritz.vision.poseestimation.FritzVisionPosePredictor;
import ai.fritz.vision.poseestimation.FritzVisionPoseResult;
import ai.fritz.vision.poseestimation.PoseOnDeviceModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Test FritzVisionImage operations
 * <p>
 * Rotations should match iOS https://developer.apple.com/documentation/uikit/uiimage/orientation/
 */
@RunWith(AndroidJUnit4.class)
public class FritzVisionImageTest extends BaseFritzTest {

    private static final String TAG = FritzVisionImageTest.class.getSimpleName();
    private static final int COORDINATE_PIXEL_BUFFER = 10;

    @Test
    public void testOrientation() {
        Bitmap bitmap = TestingAssetHelper.getBitmapForAsset(appContext, TestingAsset.FAMILY);
        FritzVisionImage testImage;
        for (ImageOrientation orientation : ImageOrientation.values()) {
            testImage = FritzVisionImage.fromBitmap(bitmap, orientation);
            ByteImage preparedBitmap = testImage.prepareBytes(testImage.getSize());
            assertNotNull(preparedBitmap);
        }
    }

    @Test
    public void testPrepareOrientation() {
        Bitmap bitmap = TestingAssetHelper.getBitmapForAsset(appContext, TestingAsset.FAMILY_ROTATE_270);
        FritzVisionImage testImage = FritzVisionImage.fromBitmap(bitmap, ImageOrientation.RIGHT);

        // Call prepare once (no resize)
        ByteImage preparedBitmap = testImage.prepareBytes();
        // Dimensions rotated 90 degrees
        assertEquals(preparedBitmap.getWidth(), testImage.getRotatedWidth());
        assertEquals(preparedBitmap.getHeight(), testImage.getRotatedHeight());

        // Call prepare again
        ByteImage preparedBitmapAgain = testImage.prepareBytes();
        // Check that the dimensions are the same as before
        assertEquals(preparedBitmap.getWidth(), bitmap.getHeight());
        assertEquals(preparedBitmap.getHeight(), bitmap.getWidth());
        assertEquals(preparedBitmap.getWidth(), preparedBitmapAgain.getWidth());
        assertEquals(preparedBitmap.getHeight(), preparedBitmapAgain.getHeight());
    }

    @Test
    public void testPredictWithSameImage() {
        Bitmap bitmap = TestingAssetHelper.getBitmapForAsset(appContext, TestingAsset.FAMILY_ROTATE_270);
        FritzVisionImage testImage = FritzVisionImage.fromBitmap(bitmap, ImageOrientation.RIGHT);

        ObjectDetectionOnDeviceModel onDeviceModel = FritzVisionModels.getObjectDetectionOnDeviceModel();
        PoseOnDeviceModel poseOnDeviceModel = FritzVisionModels.getHumanPoseEstimationOnDeviceModel(ModelVariant.ACCURATE);

        FritzVisionObjectPredictor objectPredictor = FritzVision.ObjectDetection.getPredictor(onDeviceModel);
        FritzVisionPosePredictor posePredictor = FritzVision.PoseEstimation.getPredictor(poseOnDeviceModel);

        FritzVisionObjectResult objectResult = objectPredictor.predict(testImage);
        FritzVisionPoseResult poseResult = posePredictor.predict(testImage);

        // Check that all results show up.
        ObjectResultValidator objectResultValidator = new ObjectResultValidator(objectResult);
        objectResultValidator.assertNumObjects(4);

        PoseResultValidator poseValidator = new PoseResultValidator(poseResult);
        poseValidator.assertNumPoses(1);

        // Test overlays for visual inspection
        Bitmap bboxOverlay = testImage.overlayBoundingBoxes(objectResult.getObjects());
        Bitmap poseOverlay = testImage.overlaySkeletons(poseResult.getPoses());

        assertNotNull(bboxOverlay);
        assertNotNull(poseOverlay);
    }

    @Test
    public void testFromCameraRotationBackCamera() {
        Bitmap bitmap = TestingAssetHelper.getBitmapForAsset(appContext, TestingAsset.CAT);
        ObjectDetectionOnDeviceModel onDeviceModel = FritzVisionModels.getObjectDetectionOnDeviceModel();
        FritzVisionObjectPredictor predictor = FritzVision.ObjectDetection.getPredictor(onDeviceModel);

        // Using the orientation when it's a camera rotation.
        for (ImageRotation imageRotation : ImageRotation.values()) {
            ImageOrientation orientation = FritzVisionOrientation.getOrientationByDeviceRotation(imageRotation.getRotation(), CameraCharacteristics.LENS_FACING_BACK);
            FritzVisionImage testImage = FritzVisionImage.fromBitmap(bitmap, orientation);
            FritzVisionObjectResult result = predictor.predict(testImage);
            List<FritzVisionObject> visionObjects = result.getObjects();
            Bitmap bitmapWithBBox = testImage.overlayBoundingBoxes(visionObjects);

            // For visual inspection
            assertNotNull(bitmapWithBBox);
            assertEquals(visionObjects.size(), 1);

            FritzVisionObject visionObject = visionObjects.get(0);
            assertEquals(visionObject.getVisionLabel().getText(), "cat");
            RectF bbox = visionObject.getBoundingBox();
            Log.d(TAG, orientation.name());
            Log.d(TAG, bbox.bottom + "," + bbox.left + "," + bbox.right + "," + bbox.top);

            if (orientation == ImageOrientation.UP) {
                assertTrue(withinBounds(bbox.bottom, 266.17917f));
                assertTrue(withinBounds(bbox.left, 39.864277f));
                assertTrue(withinBounds(bbox.right, 258.26712f));
                assertTrue(withinBounds(bbox.top, -2.0424843f));
            }

            if (orientation == ImageOrientation.RIGHT) {
                assertTrue(withinBounds(bbox.bottom, 280.44138f));
                assertTrue(withinBounds(bbox.left, 30.007437f));
                assertTrue(withinBounds(bbox.right, 292.113f));
                assertTrue(withinBounds(bbox.top, 38.252083f));
            }

            if (orientation == ImageOrientation.DOWN) {
                assertTrue(withinBounds(bbox.bottom, 292.48734f));
                assertTrue(withinBounds(bbox.left, 23.171595f));
                assertTrue(withinBounds(bbox.right, 257.82477f));
                assertTrue(withinBounds(bbox.top, 26.206102f));
            }

            if (orientation == ImageOrientation.LEFT) {
                assertTrue(withinBounds(bbox.bottom, 252.33661f));
                assertTrue(withinBounds(bbox.left, 5.644038f));
                assertTrue(withinBounds(bbox.right, 271.9253f));
                assertTrue(withinBounds(bbox.top, 35.513763f));
            }
        }
    }

    @Test
    public void testFromCameraRotationFrontCamera() {
        Bitmap bitmap = TestingAssetHelper.getBitmapForAsset(appContext, TestingAsset.CAT);
        ObjectDetectionOnDeviceModel onDeviceModel = FritzVisionModels.getObjectDetectionOnDeviceModel();
        FritzVisionObjectPredictor predictor = FritzVision.ObjectDetection.getPredictor(onDeviceModel);

        // Using the orientation when it's a camera rotation.
        for (ImageRotation imageRotation : ImageRotation.values()) {
            ImageOrientation orientation = FritzVisionOrientation.getOrientationByDeviceRotation(imageRotation.getRotation(), CameraCharacteristics.LENS_FACING_FRONT);
            FritzVisionImage testImage = FritzVisionImage.fromBitmap(bitmap, orientation);
            FritzVisionObjectResult result = predictor.predict(testImage);
            List<FritzVisionObject> visionObjects = result.getObjects();
            Bitmap bitmapWithBBox = testImage.overlayBoundingBoxes(visionObjects);

            // For visual inspection
            assertNotNull(bitmapWithBBox);
            assertEquals(visionObjects.size(), 1);

            FritzVisionObject visionObject = visionObjects.get(0);
            assertEquals(visionObject.getVisionLabel().getText(), "cat");
            RectF bbox = visionObject.getBoundingBox();
            Log.d(TAG, orientation.name());
            Log.d(TAG, bbox.bottom + "," + bbox.left + "," + bbox.right + "," + bbox.top);

            if (orientation == ImageOrientation.UP_MIRRORED) {
                assertTrue(withinBounds(bbox.bottom, 268.12396f));
                assertTrue(withinBounds(bbox.left, 42.36778f));
                assertTrue(withinBounds(bbox.right, 259.1906f));
                assertTrue(withinBounds(bbox.top, 6.0183673f));
            }

            if (orientation == ImageOrientation.DOWN_MIRRORED) {
                assertTrue(withinBounds(bbox.bottom, 296.32193f));
                assertTrue(withinBounds(bbox.left, 40.53045f));
                assertTrue(withinBounds(bbox.right, 267.88196f));
                assertTrue(withinBounds(bbox.top, 25.798523f));
            }

            if (orientation == ImageOrientation.RIGHT_MIRRORED) {
                assertTrue(withinBounds(bbox.bottom, 266.26593f));
                assertTrue(withinBounds(bbox.left, 24.492601f));
                assertTrue(withinBounds(bbox.right, 290.77383f));
                assertTrue(withinBounds(bbox.top, 35.29246f));
            }

            if (orientation == ImageOrientation.LEFT_MIRRORED) {
                assertTrue(withinBounds(bbox.bottom, 267.8129f));
                assertTrue(withinBounds(bbox.left, 4.304865f));
                assertTrue(withinBounds(bbox.right, 266.41046f));
                assertTrue(withinBounds(bbox.top, 44.026558f));
            }
        }
    }

    private boolean withinBounds(float actual, float expected) {
        return actual > expected - COORDINATE_PIXEL_BUFFER && actual < expected + COORDINATE_PIXEL_BUFFER;
    }

}
