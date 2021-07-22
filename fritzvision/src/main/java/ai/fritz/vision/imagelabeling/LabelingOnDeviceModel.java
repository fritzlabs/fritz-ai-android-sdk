package ai.fritz.vision.imagelabeling;

import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.core.utils.JsonUtils;


public class LabelingOnDeviceModel extends FritzOnDeviceModel {

    private List<String> labels;

    /**
     * After a successful OTA download, combine the labels with the onDeviceModel type.
     *
     * @param onDeviceModel
     * @param managedModel
     */
    public LabelingOnDeviceModel(FritzOnDeviceModel onDeviceModel, LabelingManagedModel managedModel) {
        this(onDeviceModel.getModelPath(), onDeviceModel.getModelId(), onDeviceModel.getModelVersion(), managedModel.getPinnedVersion(), managedModel.getLabels());
    }

    /**
     * Initialize the on device model with a list of labels.
     *
     * @param modelPath
     * @param modelId
     * @param modelVersion
     * @param labels
     */
    public LabelingOnDeviceModel(String modelPath, String modelId, int modelVersion, List<String> labels) {
        // By default, we assume that box coordinates for model outputs are [xmin, ymin, xmax, ymax] so default boxIndices are [0, 1, 2, 3]
        this(modelPath, modelId, modelVersion, null, labels);
    }

    /**
     * Initialize the on device model with a list of labels and a pinned version.
     *
     * @param modelPath
     * @param modelId
     * @param modelVersion
     * @param pinnedVersion
     * @param labels
     */
    public LabelingOnDeviceModel(String modelPath, String modelId, int modelVersion, Integer pinnedVersion, List<String> labels) {
        super(modelPath, modelId, modelVersion, pinnedVersion);
        this.labels = labels;
    }

    /**
     * Get a list of labels that this model predicts.
     *
     * @return a list of labels
     */
    public List<String> getLabels() {
        return labels;
    }

    public static LabelingOnDeviceModel buildFromModelConfigFile(String filePath) {
        JSONObject jsonObject = JsonUtils.buildFromJsonFile(filePath);
        try {
            String modelId = jsonObject.getString("model_id");
            String modelPath = jsonObject.getString("model_path");
            int modelVersion = jsonObject.getInt("model_version");
            Integer pinnedVersion = jsonObject.getInt("pinned_version");
            JSONArray labelsArray = jsonObject.getJSONArray("labels");
            List<String> labels = new ArrayList<>();
            for(int i=0; i<labelsArray.length(); i++) {
                labels.add(labelsArray.getString(i));
            }

            return new LabelingOnDeviceModel(modelPath, modelId, modelVersion, pinnedVersion, labels);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
