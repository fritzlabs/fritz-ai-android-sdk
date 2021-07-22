package ai.fritz.vision;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

import ai.fritz.core.annotations.AnnotatableLabel;
import ai.fritz.core.annotations.DataAnnotation;
import ai.fritz.core.annotations.KeypointAnnotation;

/**
 * A Label for the Vision Predictors.
 * <p>
 * This object holds the output for the Image Labeling feature.
 * <p>
 * It contains the label text and the confidence score.
 */
public class FritzVisionLabel implements AnnotatableLabel {
    private String text;
    private float confidence;

    public FritzVisionLabel(String text, float confidence) {
        this.text = text;
        this.confidence = confidence;
    }

    public String getText() {
        return text;
    }

    public float getConfidence() {
        return confidence;
    }

    @NotNull
    @Override
    public DataAnnotation toAnnotation() {
        return new DataAnnotation(text, new ArrayList<KeypointAnnotation>(), null, null, true);
    }
}

