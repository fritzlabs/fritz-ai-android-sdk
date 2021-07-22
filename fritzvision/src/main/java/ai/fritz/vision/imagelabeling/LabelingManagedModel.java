package ai.fritz.vision.imagelabeling;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import ai.fritz.core.FritzManagedModel;
import ai.fritz.vision.base.LabelsManager;
import ai.fritz.vision.models.LabelingModels;

/**
 * Managed model for Image Labeling Fast
 */
public class LabelingManagedModel extends FritzManagedModel {
    private List<String> labels;

    public LabelingManagedModel(String modelId, List<String> labels) {
        super(modelId);
        this.labels = labels;
    }

    public LabelingManagedModel(String modelId, Integer pinnedVersion, InputStream labelsFileInputStream) {
        super(modelId, pinnedVersion);
        this.labels = LabelsManager.loadLabels(labelsFileInputStream);
    }

    public LabelingManagedModel(String modelId, InputStream labelsFileInputStream) {
        super(modelId);
        this.labels = LabelsManager.loadLabels(labelsFileInputStream);
    }

    public List<String> getLabels() {
        return labels;
    }
}
