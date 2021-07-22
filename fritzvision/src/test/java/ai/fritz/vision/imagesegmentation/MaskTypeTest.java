package ai.fritz.vision.imagesegmentation;


import android.graphics.Color;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, packageName = "ai.fritz.sdkapp")
public class MaskTypeTest {

    @Test
    public void testChangeColor() {
        MaskClass personType = MaskClass.PERSON;
        personType.setColor(Color.YELLOW);
        assertEquals(MaskClass.PERSON.getColorIdentifier(), Color.YELLOW);
    }
}
