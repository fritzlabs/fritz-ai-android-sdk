package ai.fritz.vision.objectdetection;

import java.io.InputStream;
import java.util.List;

import ai.fritz.core.FritzManagedModel;
import ai.fritz.vision.base.LabelsManager;

/**
 * Managed model for Object Detection.
 */
public class ObjectDetectionManagedModel extends FritzManagedModel {

    private boolean isOutputNormalized;
    private Integer[] boxIndices;
    private List<String> labels;

    public ObjectDetectionManagedModel(String modelId, Integer[] boxIndices, boolean isOutputNormalized, List<String> labels) {
        this(modelId, null, boxIndices, isOutputNormalized, labels);
    }

    public ObjectDetectionManagedModel(String modelId, Integer pinnedModelVersion, Integer[] boxIndices, boolean isOutputNormalized, List<String> labels) {
        super(modelId, pinnedModelVersion);
        this.boxIndices = boxIndices;
        this.isOutputNormalized = isOutputNormalized;
        this.labels = labels;
    }

    public ObjectDetectionManagedModel(String modelId, Integer pinnedModelVersion, Integer[] boxIndices, boolean isOutputNormalized, InputStream inputStream) {
        super(modelId, pinnedModelVersion);
        this.boxIndices = boxIndices;
        this.isOutputNormalized = isOutputNormalized;
        this.labels = LabelsManager.loadLabels(inputStream);
    }

    public boolean isOutputNormalized() {
        return isOutputNormalized;
    }

    public Integer[] getBoxIndices() {
        return boxIndices;
    }

    public List<String> getLabels() {
        return labels;
    }
}