package ai.fritz.aistudio;

import android.util.Size;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import ai.fritz.aistudio.controllers.PreviewSizeController;

import static org.junit.Assert.assertEquals;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, packageName = "ai.fritz.sdkapp")
public class PreviewSizeControllerTests {
    private Size[] validPreviews = new Size[]{
            new Size(1440, 1080), // 1.33
            new Size(1280, 720), // 1.77
            new Size(1056, 704), // 1.5
            new Size(960,720), // 1.33
            new Size(864,480), // 1.8
            new Size(720,480), // 1.5
            new Size(640, 480), // 1.33
            new Size(288, 144), // 2.0
    };

    @Test(expected = RuntimeException.class)
    public void testNoResolutions() {
        PreviewSizeController controller = new PreviewSizeController(2000, 1000);
        Size[] empty = new Size[]{};
        Size testSize = controller.calculateOptimalSize(empty);
    }

    @Test
    public void testNormalRatio() {
        PreviewSizeController controller = new PreviewSizeController(1440, 960);
        Size testSize = controller.calculateOptimalSize(validPreviews);

        assertEquals(new Size(720, 480), testSize);
    }

    @Test
    public void testUniqueRatio() {
        PreviewSizeController controller = new PreviewSizeController(2960, 1440);
        Size testSize = controller.calculateOptimalSize(validPreviews);

        assertEquals(new Size(864, 480), testSize);
    }

    @Test
    public void testSmallScreen() {
        PreviewSizeController controller = new PreviewSizeController(400, 300);
        Size testSize = controller.calculateOptimalSize(validPreviews);

        assertEquals(new Size(640, 480), testSize);
    }

    @Test
    public void testLargeScreen() {
        PreviewSizeController controller = new PreviewSizeController(3840, 2160);
        Size testSize = controller.calculateOptimalSize(validPreviews);

        assertEquals(new Size(864, 480), testSize);
    }

    @Test
    public void testOutsideThreshold() {
        PreviewSizeController controller = new PreviewSizeController(1920, 1080);
        Size[] choices = new Size[]{
                new Size(1440, 1080), // 1.33
                new Size(1280, 720), // 1.77
                new Size(1056, 704), // 1.5
        };
        Size testSize = controller.calculateOptimalSize(choices);

        assertEquals(new Size(1440, 1080), testSize);
    }
}
