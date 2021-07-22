package ai.fritz.vision.objectdetection;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Size;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionObject;
import ai.fritz.vision.ImageOrientation;
import ai.fritz.vision.ImageRotation;

import static org.junit.Assert.assertEquals;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, packageName = "ai.fritz.sdkapp")
public class FritzVisionObjectResultTest {

    private Size modelInput = new Size(224, 224);

    @Test
    public void testGetVisionObjects() {
        Bitmap bitmap = Bitmap.createBitmap(300, 500, Bitmap.Config.ARGB_8888);
        FritzVisionImage visionImage = FritzVisionImage.fromBitmap(bitmap, ImageOrientation.RIGHT);

        // Confidence threshold set for prediction.
        float confidenceThreshold = .5f;

        // Create vision objects
        List<FritzVisionObject> visionObjects = new ArrayList<>();
        visionObjects.add(new FritzVisionObject("cat", .5f, new RectF(), modelInput));
        visionObjects.add(new FritzVisionObject("dog", .5f, new RectF(), modelInput));
        visionObjects.add(new FritzVisionObject("snake", .5f, new RectF(), modelInput));

        FritzVisionObjectResult objectResult = new FritzVisionObjectResult(visionObjects, confidenceThreshold, visionImage.encodedSize());

        // 3 vision object with a confidence score over .5f (by default uses the confidence score set for prediction.
        assertEquals(objectResult.getObjects().size(), 3);
    }

    @Test
    public void testGetVisionObjectsWithConfidence() {
        Bitmap bitmap = Bitmap.createBitmap(300, 500, Bitmap.Config.ARGB_8888);
        FritzVisionImage visionImage = FritzVisionImage.fromBitmap(bitmap, ImageOrientation.RIGHT);

        // Confidence threshold set for prediction.
        float confidenceThreshold = .1f;

        // Create vision objects
        List<FritzVisionObject> visionObjects = new ArrayList<>();
        visionObjects.add(new FritzVisionObject("cat", .1f, new RectF(), modelInput));
        visionObjects.add(new FritzVisionObject("dog", .2f, new RectF(), modelInput));
        visionObjects.add(new FritzVisionObject("snake", .5f, new RectF(), modelInput));

        FritzVisionObjectResult objectResult = new FritzVisionObjectResult(visionObjects, confidenceThreshold, visionImage.encodedSize());

        // Only 1 vision object with a confidence score over .5f
        assertEquals(objectResult.getObjectsAboveThreshold(.5f).size(), 1);
    }

    @Test
    public void testGetVisionObjectsByClass() {
        Bitmap bitmap = Bitmap.createBitmap(300, 500, Bitmap.Config.ARGB_8888);
        FritzVisionImage visionImage = FritzVisionImage.fromBitmap(bitmap, ImageOrientation.RIGHT);

        // Confidence threshold set for prediction.
        float confidenceThreshold = .1f;

        // Create vision objects
        List<FritzVisionObject> visionObjects = new ArrayList<>();
        visionObjects.add(new FritzVisionObject("cat", .1f, new RectF(), modelInput));
        visionObjects.add(new FritzVisionObject("dog", .2f, new RectF(), modelInput));
        visionObjects.add(new FritzVisionObject("snake", .5f, new RectF(), modelInput));

        FritzVisionObjectResult objectResult = new FritzVisionObjectResult(visionObjects, confidenceThreshold, visionImage.encodedSize());

        // Only 1 vision object matching the label "cat"
        assertEquals(objectResult.getVisionObjectsByClass("Cat").size(), 1);
    }

    @Test
    public void testGetVisionObjectsByClassAndConfidence() {
        Bitmap bitmap = Bitmap.createBitmap(300, 500, Bitmap.Config.ARGB_8888);
        FritzVisionImage visionImage = FritzVisionImage.fromBitmap(bitmap, ImageOrientation.RIGHT);

        // Confidence threshold set for prediction.
        float confidenceThreshold = .1f;

        // Create vision objects
        List<FritzVisionObject> visionObjects = new ArrayList<>();
        visionObjects.add(new FritzVisionObject("cat", .1f, new RectF(), modelInput));
        visionObjects.add(new FritzVisionObject("dog", .2f, new RectF(), modelInput));
        visionObjects.add(new FritzVisionObject("snake", .5f, new RectF(), modelInput));
        visionObjects.add(new FritzVisionObject("snake", .8f, new RectF(), modelInput));

        FritzVisionObjectResult objectResult = new FritzVisionObjectResult(visionObjects, confidenceThreshold, visionImage.encodedSize());

        // 2 vision objects that match "snake" having a confidence score higher than .3
        assertEquals(objectResult.getVisionObjectsByClass("snake", .3f).size(), 2);
    }

    @Test
    public void testGetVisionObjectsByMultiClass() {
        Bitmap bitmap = Bitmap.createBitmap(300, 500, Bitmap.Config.ARGB_8888);
        FritzVisionImage visionImage = FritzVisionImage.fromBitmap(bitmap, ImageOrientation.RIGHT);

        // Confidence threshold set for prediction.
        float confidenceThreshold = .1f;

        // Create vision objects
        List<FritzVisionObject> visionObjects = new ArrayList<>();
        visionObjects.add(new FritzVisionObject("cat", .1f, new RectF(), modelInput));
        visionObjects.add(new FritzVisionObject("dog", .2f, new RectF(), modelInput));
        visionObjects.add(new FritzVisionObject("snake", .5f, new RectF(), modelInput));
        visionObjects.add(new FritzVisionObject("snake", .8f, new RectF(), modelInput));

        FritzVisionObjectResult objectResult = new FritzVisionObjectResult(visionObjects, confidenceThreshold, visionImage.encodedSize());

        // 3 vision objects that match "snake" or "dog"
        List<String> labelNames = new ArrayList<>();
        labelNames.add("Snake");
        labelNames.add("dog");
        assertEquals(objectResult.getVisionObjectsByClasses(labelNames).size(), 3);
    }

    @Test
    public void testGetVisionObjectsByMultiClassAndConfidence() {
        Bitmap bitmap = Bitmap.createBitmap(300, 500, Bitmap.Config.ARGB_8888);
        FritzVisionImage visionImage = FritzVisionImage.fromBitmap(bitmap, ImageOrientation.RIGHT);

        // Confidence threshold set for prediction.
        float confidenceThreshold = .1f;

        // Create vision objects
        List<FritzVisionObject> visionObjects = new ArrayList<>();
        visionObjects.add(new FritzVisionObject("cat", .1f, new RectF(), modelInput));
        visionObjects.add(new FritzVisionObject("dog", .2f, new RectF(), modelInput));
        visionObjects.add(new FritzVisionObject("snake", .5f, new RectF(), modelInput));
        visionObjects.add(new FritzVisionObject("snake", .8f, new RectF(), modelInput));

        FritzVisionObjectResult objectResult = new FritzVisionObjectResult(visionObjects, confidenceThreshold, visionImage.encodedSize());

        List<String> labelNames = new ArrayList<>();
        labelNames.add("Snake");
        labelNames.add("dog");

        // 1 vision object that matches "snake" or "dog" with a confidence score above .5f
        List<FritzVisionObject> result = objectResult.getVisionObjectsByClasses(labelNames, .5f);
        assertEquals(result.size(), 2);
        for (FritzVisionObject visionObject : result) {
            assertEquals("snake", visionObject.getVisionLabel().getText());
        }

    }
}
