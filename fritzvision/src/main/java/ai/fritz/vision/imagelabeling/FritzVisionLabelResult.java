package ai.fritz.vision.imagelabeling;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import ai.fritz.core.annotations.AnnotatableResult;
import ai.fritz.core.annotations.DataAnnotation;
import ai.fritz.vision.FritzVisionLabel;

public class FritzVisionLabelResult implements AnnotatableResult {

    private static final String TAG = FritzVisionLabelResult.class.getSimpleName();

    private List<FritzVisionLabel> visionLabels;

    public FritzVisionLabelResult(List<FritzVisionLabel> visionLabels) {
        this.visionLabels = visionLabels;
    }

    public List<FritzVisionLabel> getVisionLabels() {
        return visionLabels;
    }

    public String getResultString() {
        StringBuilder strBuilder = new StringBuilder();

        for (FritzVisionLabel visionLabel : visionLabels) {
            strBuilder.append(visionLabel.getText() + " : " + visionLabel.getConfidence() + "\n");
        }

        return strBuilder.toString();
    }

    public void logResult() {
        Log.d(TAG, getResultString());
    }

    @NotNull
    @Override
    public List<DataAnnotation> toAnnotations() {
        List<DataAnnotation> annotations = new ArrayList<>();
        for (FritzVisionLabel visionLabel : visionLabels) {
            annotations.add(visionLabel.toAnnotation());
        }
        return annotations;
    }
}
