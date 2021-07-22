package ai.fritz.vision.imagesegmentation;

import ai.fritz.core.FritzManagedModel;

public class SegmentationManagedModel extends FritzManagedModel {

    private MaskClass[] classes;

    public SegmentationManagedModel(String modelId, MaskClass[] classes) {
        super(modelId);
        this.classes = classes;
    }

    public SegmentationManagedModel(String modelId, int pinnedModelVersion, MaskClass[] classes) {
        super(modelId, pinnedModelVersion);
        this.classes = classes;
    }

    public MaskClass[] getMaskClasses() {
        return classes;
    }
}
