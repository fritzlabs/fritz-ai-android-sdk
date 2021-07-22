package ai.fritz.vision.imagesegmentation;

import org.json.JSONException;
import org.json.JSONObject;

import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.core.utils.JsonUtils;

public class SegmentationOnDeviceModel extends FritzOnDeviceModel {

    private MaskClass[] maskClasses;

    public SegmentationOnDeviceModel(FritzOnDeviceModel fritzOnDeviceModel, SegmentationManagedModel managedModel) {
        this(fritzOnDeviceModel.getModelPath(), fritzOnDeviceModel.getModelId(), fritzOnDeviceModel.getModelVersion(), managedModel.getPinnedVersion(), managedModel.getMaskClasses());
    }

    public SegmentationOnDeviceModel(String modelPath, String modelId, int modelVersion, MaskClass[] maskClasses) {
        this(modelPath, modelId, modelVersion, null, maskClasses);
    }


    public SegmentationOnDeviceModel(String modelPath, String modelId, int modelVersion, Integer pinnedVersion, MaskClass[] maskClasses) {
        super(modelPath, modelId, modelVersion, pinnedVersion);
        this.maskClasses = maskClasses;
    }

    public MaskClass[] getMaskClasses() {
        return maskClasses;
    }

    public static SegmentationOnDeviceModel buildFromModelConfigFile(String filePath, MaskClass[] maskClasses) {
        JSONObject jsonObject = JsonUtils.buildFromJsonFile(filePath);
        try {
            String modelId = jsonObject.getString("model_id");
            String modelPath = jsonObject.getString("model_path");
            int modelVersion = jsonObject.getInt("model_version");
            Integer pinnedVersion = jsonObject.getInt("pinned_version");
            return new SegmentationOnDeviceModel(modelPath, modelId, modelVersion, pinnedVersion, maskClasses);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
