package ai.fritz.vision.styletransfer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Size;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import ai.fritz.vision.FritzVisionImage;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, packageName = "ai.fritz.sdkapp")
public class FritzVisionStyleResultTest {

    private static final int TEST_BITMAP_WIDTH = 16;
    private static final int TEST_BITMAP_HEIGHT = 16;
    private static final Size MODEL_SIZE = new Size(4, 4);
    private static final Size ORIGINAL_INPUT_SIZE = new Size(TEST_BITMAP_WIDTH, TEST_BITMAP_HEIGHT);
    private static final Size RESIZED_BITMAP = new Size(32, 32);

    @Test
    public void testToBitmap() {
        FritzVisionStyleResult styleResult = createTestStyleResult(false);
        Bitmap bitmap = styleResult.toBitmap();

        // to bitmap returns the model size dimensions
        assertEquals(MODEL_SIZE.getWidth(), bitmap.getWidth());
        assertEquals(MODEL_SIZE.getHeight(), bitmap.getHeight());
    }

    @Test
    public void testToBitmapTargetSize() {
        // This is old but this should return the target inference size.
        FritzVisionStyleResult styleResult = createTestStyleResult(false);
        Bitmap bitmap = styleResult.toBitmap(RESIZED_BITMAP);

        // to bitmap returns the model size dimensions
        assertEquals(RESIZED_BITMAP.getWidth(), bitmap.getWidth());
        assertEquals(RESIZED_BITMAP.getHeight(), bitmap.getHeight());
    }

    @Test
    public void testToBitmapResized() {
        // This is old but this should return the target inference size.
        FritzVisionStyleResult styleResult = createTestStyleResult(true);
        Bitmap bitmap = styleResult.toBitmap();

        // to bitmap returns the model size dimensions
        assertEquals(ORIGINAL_INPUT_SIZE.getWidth(), bitmap.getWidth());
        assertEquals(ORIGINAL_INPUT_SIZE.getHeight(), bitmap.getHeight());
    }

    private FritzVisionStyleResult createTestStyleResult(boolean resize) {
        int[] modelOutputPixles = {
                Color.BLUE, 0, 0, 0,
                0, Color.BLUE, 0, 0,
                0, 0, Color.BLUE, 0,
                0, 0, 0, Color.BLUE,
        };

        return new FritzVisionStyleResult(modelOutputPixles, MODEL_SIZE, ORIGINAL_INPUT_SIZE, resize);
    }

}
