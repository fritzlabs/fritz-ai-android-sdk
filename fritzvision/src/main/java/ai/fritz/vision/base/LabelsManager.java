package ai.fritz.vision.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import ai.fritz.core.Fritz;

public class LabelsManager {

    public static InputStream getStreamForAssetFileName(String assetFileName) {
        try {
            return Fritz.getAppContext().getAssets().open(assetFileName);
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!", e);
        }
    }

    public static List<String> loadLabels(InputStream inputStream) {
        Vector<String> labels = new Vector<>();
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line);
            }
            br.close();

            return labels;
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!", e);
        }
    }

    public static List<String> loadLabelsFromResources(int resourceId) {
        String[] labels = Fritz.getAppContext().getResources().getStringArray(resourceId);
        return Arrays.asList(labels);
    }
}
