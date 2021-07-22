package ai.fritz.vision.objectdetection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.core.utils.JsonUtils;


public class ObjectDetectionOnDeviceModel extends FritzOnDeviceModel {

    private Integer[] boxIndices;
    private boolean isOutputNormalized;
    private List<String> labels;

    public ObjectDetectionOnDeviceModel(FritzOnDeviceModel onDeviceModel, ObjectDetectionManagedModel managedModel) {
        this(onDeviceModel.getModelPath(), onDeviceModel.getModelId(), onDeviceModel.getModelVersion(), managedModel.getPinnedVersion(), managedModel.getBoxIndices(), managedModel.isOutputNormalized(), managedModel.getLabels());
    }

    public ObjectDetectionOnDeviceModel(String modelPath, String modelId, int modelVersion, boolean isOutputNormalized, List<String> labels) {
        // By default, we assume that box coordinates for model outputs are [xmin, ymin, xmax, ymax] so default boxIndices are [0, 1, 2, 3]
        this(modelPath, modelId, modelVersion, null, new Integer[]{0, 1, 2, 3}, isOutputNormalized, labels);
    }

    public ObjectDetectionOnDeviceModel(String modelPath, String modelId, int modelVersion, List<String> labels) {
        // By default, we assume that box coordinates for model outputs are [xmin, ymin, xmax, ymax] so default boxIndices are [0, 1, 2, 3]
        this(modelPath, modelId, modelVersion, null, new Integer[]{0, 1, 2, 3}, false, labels);
    }

    public ObjectDetectionOnDeviceModel(String modelPath, String modelId, int modelVersion, Integer pinnedVersion, Integer[] boxIndices, boolean isOutputNormalized, List<String> labels) {
        super(modelPath, modelId, modelVersion, pinnedVersion);
        this.boxIndices = boxIndices;
        this.isOutputNormalized = isOutputNormalized;
        this.labels = labels;
    }

    /**
     * Get index order for bounding box outputs.
     * <p>
     * By default boxes are expected to be [xmin, ymin, xmax, ymax]. If the model outputs a different order, the indices required to re-order them should be provided.
     * <p>
     *
     * @return {@link Integer[]}
     */
    public Integer[] getBoxIndices() {
        return boxIndices;
    }

    /**
     * A boolean denoting whether or not model output are in normalized or pixel coordinates.
     * <p>
     * If output is normalized, post processing will be applied to convert them to pixel coordinates.
     * <p>
     *
     * @return {@link boolean}
     */
    public boolean isOutputNormalized() {
        return isOutputNormalized;
    }


    /**
     * Get a list of labels that this model predicts.
     *
     * @return a list of labels
     */
    public List<String> getLabels() {
        return labels;
    }

    public static ObjectDetectionOnDeviceModel buildFromModelConfigFile(String filePath) {
        JSONObject jsonObject = JsonUtils.buildFromJsonFile(filePath);
        try {
            String modelId = jsonObject.getString("model_id");
            String modelPath = jsonObject.getString("model_path");
            int modelVersion = jsonObject.getInt("model_version");
            Integer pinnedVersion = jsonObject.getInt("pinned_version");
            JSONArray boxIndiceArray = jsonObject.getJSONArray("boxIndices");
            Integer[] boxIndices = new Integer[]{
                    boxIndiceArray.getInt(0),
                    boxIndiceArray.getInt(1),
                    boxIndiceArray.getInt(2),
                    boxIndiceArray.getInt(3),
            };
            boolean isOutputNormalized = jsonObject.getBoolean("isOutputNormalized");
            JSONArray labelsArray = jsonObject.getJSONArray("labels");
            List<String> labels = new ArrayList<>();
            for (int i = 0; i < labelsArray.length(); i++) {
                labels.add(labelsArray.getString(i));
            }

            return new ObjectDetectionOnDeviceModel(modelPath, modelId, modelVersion, pinnedVersion, boxIndices, isOutputNormalized, labels);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
