package ai.fritz.vision.poseestimation;

import org.json.JSONException;
import org.json.JSONObject;

import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.core.utils.JsonUtils;

public class PoseOnDeviceModel extends FritzOnDeviceModel {

    private Skeleton skeleton;
    private boolean useDisplacements;
    private int outputStride;

    public PoseOnDeviceModel(FritzOnDeviceModel onDeviceModel, PoseManagedModel managedModel) {
        this(onDeviceModel.getModelPath(), onDeviceModel.getModelId(), onDeviceModel.getModelVersion(),
                managedModel.getPinnedVersion(), managedModel.getSkeleton(), managedModel.getOutputStride(), managedModel.useDisplacements());
    }

    public PoseOnDeviceModel(String modelPath, String modelId, int modelVersion, Skeleton skeleton, int outputStride) {
        this(modelPath, modelId, modelVersion, null, skeleton, outputStride, true);
    }

    public PoseOnDeviceModel(String modelPath, String modelId, int modelVersion, Skeleton skeleton, int outputStride, boolean useDisplacements) {
        this(modelPath, modelId, modelVersion, null, skeleton, outputStride, useDisplacements);
    }

    public PoseOnDeviceModel(String modelPath, String modelId, int modelVersion, Integer pinnedVersion, Skeleton skeleton, int outputStride, boolean useDisplacements) {
        super(modelPath, modelId, modelVersion, pinnedVersion);
        this.skeleton = skeleton;
        this.useDisplacements = useDisplacements;
        this.outputStride = outputStride;
    }

    public boolean useDisplacements() {
        return useDisplacements;
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public int getOutputStride() {
        return outputStride;
    }

    public static PoseOnDeviceModel buildFromModelConfigFile(String filePath, Skeleton skeleton) {
        JSONObject jsonObject = JsonUtils.buildFromJsonFile(filePath);
        try {
            String modelId = jsonObject.getString("model_id");
            String modelPath = jsonObject.getString("model_path");
            int modelVersion = jsonObject.getInt("model_version");
            Integer pinnedVersion = jsonObject.getInt("pinned_version");
            int outputStride = jsonObject.getInt("output_stride");
            boolean useDisplacements = jsonObject.getBoolean("use_displacements");
            return new PoseOnDeviceModel(modelPath, modelId, modelVersion, pinnedVersion, skeleton, outputStride, useDisplacements);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
