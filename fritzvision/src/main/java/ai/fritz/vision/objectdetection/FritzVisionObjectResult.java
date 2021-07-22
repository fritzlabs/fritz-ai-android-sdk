package ai.fritz.vision.objectdetection;

import android.util.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import ai.fritz.core.annotations.DataAnnotation;
import ai.fritz.core.annotations.AnnotatableResult;
import ai.fritz.vision.FritzVisionLabel;
import ai.fritz.vision.FritzVisionObject;

public class FritzVisionObjectResult implements AnnotatableResult {

    private static final String TAG = FritzVisionObjectResult.class.getSimpleName();

    private List<FritzVisionObject> visionObjects;
    private float confidenceThreshold;
    private Size sourceInputSize;

    public FritzVisionObjectResult(List<FritzVisionObject> visionObjects, float confidenceThreshold, Size sourceInputSize) {
        this.visionObjects = visionObjects;
        this.confidenceThreshold = confidenceThreshold;
        this.sourceInputSize = sourceInputSize;
    }

    /**
     * Get objects.
     *
     * @return a list of objects.
     */
    public List<FritzVisionObject> getObjects() {
        return getObjectsAboveThreshold(confidenceThreshold);
    }

    /**
     * Get objects above a given confidence threshold.
     *
     * @param confidenceThreshold -  the confidence threshold
     * @return a list of objects above the confidence threshold.
     */
    public List<FritzVisionObject> getObjectsAboveThreshold(float confidenceThreshold) {
        List<FritzVisionObject> results = new ArrayList<>();

        for (FritzVisionObject visionObject : visionObjects) {
            if (visionObject.getVisionLabel().getConfidence() >= confidenceThreshold) {
                results.add(visionObject);
            }
        }

        return results;
    }

    /**
     * Gets objects with a certain class label.
     *
     * @param labelName - the label to look for.
     * @return the objects with the given label.
     */
    public List<FritzVisionObject> getVisionObjectsByClass(String labelName) {
        return getVisionObjectsByClass(labelName, confidenceThreshold);
    }

    /**
     * Gets objects with a certain class label and confidence.
     *
     * @param labelName       - the label to look for.
     * @param labelConfidence - the confidence threshold.
     * @return the objects over the threshold with the given label.
     */
    public List<FritzVisionObject> getVisionObjectsByClass(String labelName, float labelConfidence) {

        List<FritzVisionObject> results = new ArrayList<>();

        for (FritzVisionObject visionObject : visionObjects) {
            FritzVisionLabel label = visionObject.getVisionLabel();
            if (label.getText().equalsIgnoreCase(labelName) && label.getConfidence() >= labelConfidence) {
                results.add(visionObject);
            }
        }

        return results;
    }

    /**
     * Gets objects matching the labels provided
     *
     * @param labelNames - the labels to look for.
     * @return the objects
     */
    public List<FritzVisionObject> getVisionObjectsByClasses(List<String> labelNames) {
        return getVisionObjectsByClasses(labelNames, confidenceThreshold);
    }

    /**
     * Gets objects matching the labels provided and confidence threshold
     *
     * @param labelNames      - the labels to look for.
     * @param labelConfidence - the confidence threshold.
     * @return the objects
     */
    public List<FritzVisionObject> getVisionObjectsByClasses(List<String> labelNames, float labelConfidence) {

        // Convert label names to lower case
        ListIterator<String> iterator = labelNames.listIterator();
        while (iterator.hasNext()) {
            iterator.set(iterator.next().toLowerCase());
        }

        List<FritzVisionObject> results = new ArrayList<>();

        // Add vision object if its label is in the list of names that we're searching for
        for (FritzVisionObject visionObject : visionObjects) {
            FritzVisionLabel label = visionObject.getVisionLabel();
            if (labelNames.contains(label.getText().toLowerCase()) && label.getConfidence() >= labelConfidence) {
                results.add(visionObject);
            }
        }

        return results;
    }

    @Override
    public List<DataAnnotation> toAnnotations() {
        List<DataAnnotation> annotations = new ArrayList<>();
        for (FritzVisionObject object : visionObjects) {
            annotations.add(object.toAnnotation(sourceInputSize));
        }
        return annotations;
    }
}
