package ai.fritz.vision.imagelabeling;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import ai.fritz.vision.FritzVisionLabel;

import static org.junit.Assert.assertEquals;

public class FritzVisionLabelResultTest {

    @Test
    public void testGetVisionLabels() {
        List<FritzVisionLabel> labels = new ArrayList<>();
        labels.add(new FritzVisionLabel("dog", .5f));
        FritzVisionLabelResult labelResult = new FritzVisionLabelResult(labels);
        assertEquals(labels, labelResult.getVisionLabels());
    }

    @Test
    public void testGetResultString() {
        List<FritzVisionLabel> labels = new ArrayList<>();
        labels.add(new FritzVisionLabel("dog", .5f));
        labels.add(new FritzVisionLabel("cat", .3f));
        FritzVisionLabelResult labelResult = new FritzVisionLabelResult(labels);

        String expectedString = "dog : 0.5\ncat : 0.3\n";

        assertEquals(expectedString, labelResult.getResultString());
    }
}
