package ai.fritz.vision;

import ai.fritz.core.FritzManagedModel;
import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.core.ModelReadyListener;
import ai.fritz.core.utils.FritzModelManager;
import ai.fritz.vision.base.FeatureBase;
import ai.fritz.vision.imagelabeling.FritzVisionLabelPredictor;
import ai.fritz.vision.imagelabeling.FritzVisionLabelPredictorOptions;
import ai.fritz.vision.imagelabeling.LabelingManagedModel;
import ai.fritz.vision.imagelabeling.LabelingOnDeviceModel;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictor;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictorOptions;
import ai.fritz.vision.imagesegmentation.SegmentationManagedModel;
import ai.fritz.vision.imagesegmentation.SegmentationOnDeviceModel;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictor;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictorOptions;
import ai.fritz.vision.objectdetection.ObjectDetectionManagedModel;
import ai.fritz.vision.objectdetection.ObjectDetectionOnDeviceModel;
import ai.fritz.vision.poseestimation.FritzVisionPosePredictor;
import ai.fritz.vision.poseestimation.FritzVisionPosePredictorOptions;
import ai.fritz.vision.poseestimation.PoseManagedModel;
import ai.fritz.vision.poseestimation.PoseOnDeviceModel;
import ai.fritz.vision.styletransfer.FritzVisionStylePredictor;
import ai.fritz.vision.styletransfer.FritzVisionStylePredictorOptions;


public class FritzVision {

    public static ImageLabelingFeature ImageLabeling = new ImageLabelingFeature();
    public static ImageSegmentationFeature ImageSegmentation = new ImageSegmentationFeature();
    public static ObjectDetectionFeature ObjectDetection = new ObjectDetectionFeature();
    public static StyleTransferFeature StyleTransfer = new StyleTransferFeature();
    public static PoseEstimationFeature PoseEstimation = new PoseEstimationFeature();

    /**
     * Preload image processing contexts to speed up first time inference.
     */
    public static void preload() {
        // Creates the processing
        ProcessingContext.getInstance();
    }


    public static class ImageLabelingFeature extends FeatureBase<FritzVisionLabelPredictor, FritzVisionLabelPredictorOptions, LabelingManagedModel, LabelingOnDeviceModel> {

        @Override
        protected FritzVisionLabelPredictorOptions getDefaultOptions() {
            FritzVisionLabelPredictorOptions options = new FritzVisionLabelPredictorOptions();
            return options;
        }

        @Override
        public FritzVisionLabelPredictor getPredictor(LabelingOnDeviceModel onDeviceModel, FritzVisionLabelPredictorOptions options) {
            return new FritzVisionLabelPredictor(onDeviceModel, options);
        }

        @Override
        public void loadPredictor(final LabelingManagedModel managedModel, final FritzVisionLabelPredictorOptions options, final PredictorStatusListener statusListener, boolean useWifi) {
            final FritzModelManager modelManager = new FritzModelManager(managedModel);
            modelManager.loadModel(new ModelReadyListener() {
                @Override
                public void onModelReady(FritzOnDeviceModel onDeviceModel) {
                    LabelingOnDeviceModel labelingOnDeviceModel = new LabelingOnDeviceModel(onDeviceModel, managedModel);
                    FritzVisionLabelPredictor predictor = new FritzVisionLabelPredictor(labelingOnDeviceModel, options);
                    statusListener.onPredictorReady(predictor);
                }
            }, useWifi);
        }
    }

    public static class ImageSegmentationFeature extends FeatureBase<FritzVisionSegmentationPredictor, FritzVisionSegmentationPredictorOptions, SegmentationManagedModel, SegmentationOnDeviceModel> {
        @Override
        protected FritzVisionSegmentationPredictorOptions getDefaultOptions() {
            return new FritzVisionSegmentationPredictorOptions();
        }

        @Override
        public FritzVisionSegmentationPredictor getPredictor(SegmentationOnDeviceModel onDeviceModel, FritzVisionSegmentationPredictorOptions options) {
            return new FritzVisionSegmentationPredictor(onDeviceModel, options);
        }

        @Override
        public void loadPredictor(final SegmentationManagedModel managedModel, final FritzVisionSegmentationPredictorOptions options, final PredictorStatusListener statusListener, boolean useWifi) {
            final FritzModelManager modelManager = new FritzModelManager(managedModel);
            modelManager.loadModel(new ModelReadyListener() {
                @Override
                public void onModelReady(FritzOnDeviceModel onDeviceModel) {
                    SegmentationOnDeviceModel imageSegModel = new SegmentationOnDeviceModel(onDeviceModel, managedModel);
                    FritzVisionSegmentationPredictor predictor = new FritzVisionSegmentationPredictor(imageSegModel, options);
                    statusListener.onPredictorReady(predictor);
                }
            }, useWifi);
        }
    }

    public static class ObjectDetectionFeature extends FeatureBase<FritzVisionObjectPredictor, FritzVisionObjectPredictorOptions, ObjectDetectionManagedModel, ObjectDetectionOnDeviceModel> {

        @Override
        protected FritzVisionObjectPredictorOptions getDefaultOptions() {
            return new FritzVisionObjectPredictorOptions();
        }

        @Override
        public FritzVisionObjectPredictor getPredictor(ObjectDetectionOnDeviceModel onDeviceModel, FritzVisionObjectPredictorOptions options) {
            return new FritzVisionObjectPredictor(onDeviceModel, options);
        }

        @Override
        public void loadPredictor(final ObjectDetectionManagedModel managedModel, final FritzVisionObjectPredictorOptions options, final PredictorStatusListener statusListener, boolean useWifi) {
            final FritzModelManager modelManager = new FritzModelManager(managedModel);
            modelManager.loadModel(new ModelReadyListener() {
                @Override
                public void onModelReady(FritzOnDeviceModel onDeviceModel) {
                    ObjectDetectionOnDeviceModel objectDetectionOnDeviceModel = new ObjectDetectionOnDeviceModel(onDeviceModel, managedModel);
                    FritzVisionObjectPredictor predictor = new FritzVisionObjectPredictor(objectDetectionOnDeviceModel, options);
                    statusListener.onPredictorReady(predictor);
                }
            }, useWifi);
        }
    }

    public static class StyleTransferFeature extends FeatureBase<FritzVisionStylePredictor, FritzVisionStylePredictorOptions, FritzManagedModel, FritzOnDeviceModel> {

        @Override
        protected FritzVisionStylePredictorOptions getDefaultOptions() {
            return new FritzVisionStylePredictorOptions();
        }

        @Override
        public FritzVisionStylePredictor getPredictor(FritzOnDeviceModel styleOnDeviceModel, FritzVisionStylePredictorOptions options) {
            return new FritzVisionStylePredictor(styleOnDeviceModel, options);
        }

        @Override
        public void loadPredictor(final FritzManagedModel managedModel, final FritzVisionStylePredictorOptions options, final PredictorStatusListener statusListener, boolean useWifi) {
            final FritzModelManager modelManager = new FritzModelManager(managedModel);
            modelManager.loadModel(new ModelReadyListener() {
                @Override
                public void onModelReady(FritzOnDeviceModel onDeviceModel) {
                    FritzVisionStylePredictor predictor = new FritzVisionStylePredictor(onDeviceModel, options);
                    statusListener.onPredictorReady(predictor);
                }
            }, useWifi);
        }
    }

    public static class PoseEstimationFeature extends FeatureBase<FritzVisionPosePredictor, FritzVisionPosePredictorOptions, PoseManagedModel, PoseOnDeviceModel> {

        @Override
        protected FritzVisionPosePredictorOptions getDefaultOptions() {
            return new FritzVisionPosePredictorOptions();
        }

        @Override
        public FritzVisionPosePredictor getPredictor(PoseOnDeviceModel onDeviceModel, FritzVisionPosePredictorOptions options) {
            return new FritzVisionPosePredictor(onDeviceModel, options);
        }

        @Override
        public void loadPredictor(final PoseManagedModel managedModel, final FritzVisionPosePredictorOptions options, final PredictorStatusListener statusListener, boolean useWifi) {
            final FritzModelManager modelManager = new FritzModelManager(managedModel);
            modelManager.loadModel(new ModelReadyListener() {
                @Override
                public void onModelReady(FritzOnDeviceModel onDeviceModel) {
                    PoseOnDeviceModel poseOnDeviceModel = new PoseOnDeviceModel(onDeviceModel, managedModel);
                    FritzVisionPosePredictor predictor = new FritzVisionPosePredictor(poseOnDeviceModel, options);
                    statusListener.onPredictorReady(predictor);
                }
            }, useWifi);
        }
    }
}
