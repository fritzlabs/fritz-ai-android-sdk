package ai.fritz.sdktests;

import org.junit.Test;

import java.io.InputStream;
import java.util.List;
import static org.junit.Assert.assertEquals;

import ai.fritz.sdktests.BaseFritzTest;
import ai.fritz.sdktests.R;
import ai.fritz.vision.base.LabelsManager;

public class LabelsManagerTests extends BaseFritzTest {

    @Test
    public void testloadLabelsFromAssetsFile() {
        InputStream assetFile = LabelsManager.getStreamForAssetFileName("labels.txt");
        List<String> labels = LabelsManager.loadLabels(assetFile);
        assertEquals(labels.size(), 10);
    }

    @Test
    public void testLoadLabelsFromResource() {
        List<String> labels = LabelsManager.loadLabelsFromResources(R.array.labels);
        assertEquals(labels.size(), 10);
    }
}
