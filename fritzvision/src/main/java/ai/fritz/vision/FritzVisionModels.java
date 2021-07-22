package ai.fritz.vision;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import ai.fritz.core.Fritz;
import ai.fritz.core.FritzManagedModel;
import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.vision.base.LabelsManager;
import ai.fritz.vision.imagelabeling.LabelingManagedModel;
import ai.fritz.vision.imagelabeling.LabelingOnDeviceModel;
import ai.fritz.vision.imagesegmentation.MaskClass;
import ai.fritz.vision.imagesegmentation.SegmentationClasses;
import ai.fritz.vision.imagesegmentation.SegmentationManagedModel;
import ai.fritz.vision.imagesegmentation.SegmentationOnDeviceModel;
import ai.fritz.vision.models.DetectionModels;
import ai.fritz.vision.models.LabelingModels;
import ai.fritz.vision.models.PoseModels;
import ai.fritz.vision.models.SegmentationModels;
import ai.fritz.vision.objectdetection.ObjectDetectionManagedModel;
import ai.fritz.vision.objectdetection.ObjectDetectionOnDeviceModel;
import ai.fritz.vision.poseestimation.HumanSkeleton;
import ai.fritz.vision.poseestimation.PoseManagedModel;
import ai.fritz.vision.poseestimation.PoseOnDeviceModel;
import ai.fritz.vision.styletransfer.PaintingStyleModels;
import ai.fritz.vision.styletransfer.PatternStyleModels;


public class FritzVisionModels {

    private static final int HAIR_SEG_ACCURATE_PINNED_VERSION = 8;
    private static final int HAIR_SEG_FAST_PINNED_VERSION = 15;
    private static final int HAIR_SEG_SMALL_PINNED_VERSION = 2;

    private static final int SKY_SEG_ACCURATE_PINNED_VERSION = 6;
    private static final int SKY_SEG_FAST_PINNED_VERSION = 5;
    private static final int SKY_SEG_SMALL_PINNED_VERSION = 2;

    private static final int PET_SEG_ACCURATE_PINNED_VERSION = 6;
    private static final int PET_SEG_FAST_PINNED_VERSION = 8;
    private static final int PET_SEG_SMALL_PINNED_VERSION = 2;

    private static final int LIVING_ROOM_SEG_FAST_PINNED_VERSION = 1;
    private static final int LIVING_ROOM_SEG_SMALL_PINNED_VERSION = 2;

    private static final int OUTDOOR_SEG_ACCURATE_PINNED_VERSION = 5;
    private static final int OUTDOOR_SEG_FAST_PINNED_VERSION = 6;
    private static final int OUTDOOR_SEG_SMALL_PINNED_VERSION = 2;

    private static final int PEOPLE_SEG_ACCURATE_PINNED_VERSION = 1;
    private static final int PEOPLE_SEG_FAST_PINNED_VERSION = 1;
    private static final int PEOPLE_SEG_SMALL_PINNED_VERSION = 1;

    private static final int POSE_ACCURATE_PINNED_VERSION = 1;
    private static final int POSE_FAST_PINNED_VERSION = 1;
    private static final int POSE_SMALL_PINNED_VERSION = 1;

    private static final int OBJECT_PINNED_VERSION = 1;
    private static final int LABELING_PINNED_VERSION = 1;


    public static SegmentationOnDeviceModel getHairSegmentationOnDeviceModel(ModelVariant modelVariant) {
        String assetName = "hair_seg_" + modelVariant.name().toLowerCase() + ".json";
        checkExists(assetName, "Hair Segmentation Model (" + modelVariant.name() + ")",
                "vision-hair-segmentation-model-" + modelVariant.name().toLowerCase());

        SegmentationOnDeviceModel onDeviceModel = SegmentationOnDeviceModel.buildFromModelConfigFile(
                assetName, SegmentationClasses.HAIR);
        return onDeviceModel;
    }

    public static SegmentationOnDeviceModel getSkySegmentationOnDeviceModel(ModelVariant modelVariant) {
        String assetName = "sky_seg_" + modelVariant.name().toLowerCase() + ".json";
        checkExists(assetName, "Sky Segmentation Model (" + modelVariant.name() + ")",
                "vision-sky-segmentation-model-" + modelVariant.name().toLowerCase());

        SegmentationOnDeviceModel onDeviceModel = SegmentationOnDeviceModel.buildFromModelConfigFile(
                assetName, SegmentationClasses.SKY);
        return onDeviceModel;
    }

    public static SegmentationOnDeviceModel getLivingRoomSegmentationOnDeviceModel(ModelVariant modelVariant) {
        String assetName = "living_room_seg_" + modelVariant.name().toLowerCase() + ".json";
        checkExists(assetName, "Living Room Segmentation Model (" + modelVariant.name() + ")",
                "vision-living-room-segmentation-model-" + modelVariant.name().toLowerCase());

        SegmentationOnDeviceModel onDeviceModel = SegmentationOnDeviceModel.buildFromModelConfigFile(
                assetName, SegmentationClasses.LIVING_ROOM);
        return onDeviceModel;
    }

    public static SegmentationOnDeviceModel getOutdoorSegmentationOnDeviceModel(ModelVariant modelVariant) {
        String assetName = "outdoor_seg_" + modelVariant.name().toLowerCase() + ".json";
        checkExists(assetName, "Outdoor Segmentation Model (" + modelVariant.name() + ")",
                "vision-outdoor-segmentation-model-" + modelVariant.name().toLowerCase());
        SegmentationOnDeviceModel onDeviceModel = SegmentationOnDeviceModel.buildFromModelConfigFile(
                assetName, SegmentationClasses.OUTDOOR);
        return onDeviceModel;
    }

    public static SegmentationOnDeviceModel getPetSegmentationOnDeviceModel(ModelVariant modelVariant) {
        String assetName = "pet_seg_" + modelVariant.name().toLowerCase() + ".json";
        checkExists(assetName, "Pet Segmentation Model (" + modelVariant.name() + ")",
                "vision-pet-segmentation-model-" + modelVariant.name().toLowerCase());
        SegmentationOnDeviceModel onDeviceModel = SegmentationOnDeviceModel.buildFromModelConfigFile(
                assetName, SegmentationClasses.PET);
        return onDeviceModel;
    }

    public static SegmentationOnDeviceModel getPeopleSegmentationOnDeviceModel(ModelVariant modelVariant) {
        String assetName = "people_seg_" + modelVariant.name().toLowerCase() + ".json";
        checkExists(assetName, "People Segmentation Model (" + modelVariant.name() + ")",
                "vision-people-segmentation-model-" + modelVariant.name().toLowerCase());
        SegmentationOnDeviceModel onDeviceModel = SegmentationOnDeviceModel.buildFromModelConfigFile(
                assetName, SegmentationClasses.PEOPLE);
        return onDeviceModel;
    }

    public static PoseOnDeviceModel getHumanPoseEstimationOnDeviceModel(ModelVariant modelVariant) {
        String assetName = "pose_estimation_" + modelVariant.name().toLowerCase() + ".json";
        checkExists(assetName,
                "Pose Estimation Model (" + modelVariant.name() + ")",
                "vision-pose-estimation-model-" + modelVariant.name().toLowerCase());
        return PoseOnDeviceModel.buildFromModelConfigFile(assetName, new HumanSkeleton());
    }

    public static ObjectDetectionOnDeviceModel getObjectDetectionOnDeviceModel() {
        String modelConfigPath = "object_detection_fast.json";
        checkExists(modelConfigPath, "Object Detection Model", "vision-object-detection-model-fast");
        ObjectDetectionOnDeviceModel onDeviceModel = ObjectDetectionOnDeviceModel.buildFromModelConfigFile(modelConfigPath);
        return onDeviceModel;
    }

    public static LabelingOnDeviceModel getImageLabelingOnDeviceModel() {
        String modelConfigPath = "labeling_fast.json";
        checkExists(modelConfigPath, "Image Labeling Model", "vision-labeling-model-fast");
        return LabelingOnDeviceModel.buildFromModelConfigFile(modelConfigPath);
    }

    public static PaintingStyleModels getPaintingStyleModels() {
        String paintingModelsPath = "painting_style_models.json";
        checkExists(paintingModelsPath, "Painting Style Models", "vision-style-painting-models");
        return new PaintingStyleModels(paintingModelsPath);
    }

    public static PatternStyleModels getPatternStyleModels() {
        String patternStyleModelPath = "pattern_style_models.json";
        checkExists(patternStyleModelPath, "Pattern Style Models", "vision-style-painting-models");
        return new PatternStyleModels(patternStyleModelPath);
    }

    public static SegmentationManagedModel getHairSegmentationManagedModel(ModelVariant modelVariant) {
        switch (modelVariant) {
            case ACCURATE:
                return new SegmentationManagedModel( SegmentationModels.HAIR_ACCURATE_MODEL_ID, HAIR_SEG_ACCURATE_PINNED_VERSION, SegmentationClasses.HAIR);
            case SMALL:
                return new SegmentationManagedModel(SegmentationModels.HAIR_SMALL_MODEL_ID, HAIR_SEG_SMALL_PINNED_VERSION, SegmentationClasses.HAIR);
            default:
                return new SegmentationManagedModel(SegmentationModels.HAIR_FAST_MODEL_ID, HAIR_SEG_FAST_PINNED_VERSION, SegmentationClasses.HAIR);
        }
    }

    public static SegmentationManagedModel getSkySegmentationManagedModel(ModelVariant modelVariant) {
        switch (modelVariant) {
            case ACCURATE:
                return new SegmentationManagedModel( SegmentationModels.SKY_ACCURATE_MODEL_ID, SKY_SEG_ACCURATE_PINNED_VERSION, SegmentationClasses.SKY);
            case SMALL:
                return new SegmentationManagedModel(SegmentationModels.SKY_SMALL_MODEL_ID, SKY_SEG_SMALL_PINNED_VERSION, SegmentationClasses.SKY);
            default:
                return new SegmentationManagedModel(SegmentationModels.SKY_FAST_MODEL_ID, SKY_SEG_FAST_PINNED_VERSION, SegmentationClasses.SKY);
        }
    }

    public static SegmentationManagedModel getPetSegmentationManagedModel(ModelVariant modelVariant) {
        switch (modelVariant) {
            case ACCURATE:
                return new SegmentationManagedModel( SegmentationModels.PET_ACCURATE_MODEL_ID, PET_SEG_ACCURATE_PINNED_VERSION, SegmentationClasses.PET);
            case SMALL:
                return new SegmentationManagedModel(SegmentationModels.PET_SMALL_MODEL_ID, PET_SEG_SMALL_PINNED_VERSION, SegmentationClasses.PET);
            default:
                return new SegmentationManagedModel(SegmentationModels.PET_FAST_MODEL_ID, PET_SEG_FAST_PINNED_VERSION, SegmentationClasses.PET);
        }
    }

    public static SegmentationManagedModel getOutdoorSegmentationManagedModel(ModelVariant modelVariant) {
        switch (modelVariant) {
            case ACCURATE:
                return new SegmentationManagedModel( SegmentationModels.OUTDOOR_ACCURATE_MODEL_ID, OUTDOOR_SEG_ACCURATE_PINNED_VERSION, SegmentationClasses.OUTDOOR);
            case SMALL:
                return new SegmentationManagedModel(SegmentationModels.OUTDOOR_SMALL_MODEL_ID, OUTDOOR_SEG_SMALL_PINNED_VERSION, SegmentationClasses.OUTDOOR);
            default:
                return new SegmentationManagedModel(SegmentationModels.OUTDOOR_FAST_MODEL_ID, OUTDOOR_SEG_FAST_PINNED_VERSION, SegmentationClasses.OUTDOOR);
        }
    }

    public static SegmentationManagedModel getLivingRoomSegmentationManagedModel(ModelVariant modelVariant) {
        switch (modelVariant) {
            case SMALL:
                return new SegmentationManagedModel(SegmentationModels.LIVING_ROOM_SMALL_MODEL_ID, LIVING_ROOM_SEG_SMALL_PINNED_VERSION, SegmentationClasses.LIVING_ROOM);
            default:
                return new SegmentationManagedModel(SegmentationModels.LIVING_ROOM_FAST_MODEL_ID, LIVING_ROOM_SEG_FAST_PINNED_VERSION, SegmentationClasses.LIVING_ROOM);
        }
    }

    public static SegmentationManagedModel getPeopleSegmentationManagedModel(ModelVariant modelVariant) {
        switch (modelVariant) {
            case ACCURATE:
                return new SegmentationManagedModel( SegmentationModels.PEOPLE_ACCURATE_MODEL_ID, PEOPLE_SEG_ACCURATE_PINNED_VERSION, SegmentationClasses.PEOPLE);
            case SMALL:
                return new SegmentationManagedModel(SegmentationModels.PEOPLE_SMALL_MODEL_ID, PEOPLE_SEG_SMALL_PINNED_VERSION, SegmentationClasses.PEOPLE);
            default:
                return new SegmentationManagedModel(SegmentationModels.PEOPLE_FAST_MODEL_ID, PEOPLE_SEG_FAST_PINNED_VERSION, SegmentationClasses.PEOPLE);
        }
    }

    public static PoseManagedModel getHumanPoseEstimationManagedModel(ModelVariant modelVariant) {
        switch (modelVariant) {
            case ACCURATE:
                return new PoseManagedModel(PoseModels.ACCURATE_MODEL_ID, POSE_ACCURATE_PINNED_VERSION, new HumanSkeleton(), 8, true);
            case SMALL:
                return new PoseManagedModel(PoseModels.SMALL_MODEL_ID, POSE_SMALL_PINNED_VERSION, new HumanSkeleton(), 8, true);
            default:
                return new PoseManagedModel(PoseModels.FAST_MODEL_ID, POSE_FAST_PINNED_VERSION, new HumanSkeleton(), 8, true);
        }
    }

    public static ObjectDetectionManagedModel getObjectDetectionManagedModel() {
        InputStream inputStream = LabelsManager.getStreamForAssetFileName("coco_labels_list.txt");
        return new ObjectDetectionManagedModel(DetectionModels.MODEL_ID, OBJECT_PINNED_VERSION, new Integer[]{1, 0, 3, 2}, true, inputStream);
    }

    public static LabelingManagedModel getImageLabelingManagedModel() {
        InputStream inputStream = LabelsManager.getStreamForAssetFileName("mobilenet_labels.txt");
        return new LabelingManagedModel(LabelingModels.FAST_MODEL_ID, LABELING_PINNED_VERSION, inputStream);
    }

    private static void checkExists(String assetsFile, String modelName, String dependencyName) {
        try {
            if (!Arrays.asList(Fritz.getAppContext().getResources().getAssets().list("")).contains(assetsFile)) {
                throw new RuntimeException(
                        "You have not included " + modelName + " in the your app/build.gradle dependencies.\n\n" +
                                "dependencies {\n" +
                                "   implementation \"ai.fritz:" + dependencyName + ":+\"\n" +
                                "}\n\n" +
                                "Please add and resync your project ");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
