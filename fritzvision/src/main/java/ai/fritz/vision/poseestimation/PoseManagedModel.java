package ai.fritz.vision.poseestimation;

import ai.fritz.core.FritzManagedModel;

public class PoseManagedModel extends FritzManagedModel {

    private Skeleton skeleton;
    private boolean useDisplacements;
    private int outputStride;

    public PoseManagedModel(String modelId, Skeleton skeleton, int outputStride, boolean useDisplacements) {
        this(modelId, null, skeleton, outputStride, useDisplacements);
    }

    public PoseManagedModel(String modelId, Integer pinnedModelVersion, Skeleton skeleton, int outputStride, boolean useDisplacements) {
        super(modelId, pinnedModelVersion);
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
}
