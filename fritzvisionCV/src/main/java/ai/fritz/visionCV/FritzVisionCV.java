package ai.fritz.visionCV;

import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.core.ModelReadyListener;
import ai.fritz.core.utils.FritzModelManager;
import ai.fritz.vision.PredictorStatusListener;
import ai.fritz.vision.base.FeatureBase;
import ai.fritz.visionCV.rigidpose.FritzVisionRigidPosePredictor;
import ai.fritz.visionCV.rigidpose.FritzVisionRigidPosePredictorOptions;
import ai.fritz.visionCV.rigidpose.RigidPoseManagedModel;
import ai.fritz.visionCV.rigidpose.RigidPoseOnDeviceModel;


public class FritzVisionCV {

    public static RigidPoseFeature RigidPose = new RigidPoseFeature();

    public static class RigidPoseFeature extends FeatureBase<FritzVisionRigidPosePredictor, FritzVisionRigidPosePredictorOptions, RigidPoseManagedModel, RigidPoseOnDeviceModel> {
        @Override
        protected FritzVisionRigidPosePredictorOptions getDefaultOptions() {
            FritzVisionRigidPosePredictorOptions options = new FritzVisionRigidPosePredictorOptions();
            return options;
        }

        @Override
        public FritzVisionRigidPosePredictor getPredictor(RigidPoseOnDeviceModel onDeviceModel, FritzVisionRigidPosePredictorOptions options) {
            return new FritzVisionRigidPosePredictor(onDeviceModel, options);
        }

        @Override
        public void loadPredictor(final RigidPoseManagedModel managedModel, final FritzVisionRigidPosePredictorOptions options, final PredictorStatusListener statusListener, boolean useWifi) {
            final FritzModelManager modelManager = new FritzModelManager(managedModel);
            modelManager.loadModel(new ModelReadyListener() {
                @Override
                public void onModelReady(FritzOnDeviceModel onDeviceModel) {
                    RigidPoseOnDeviceModel rigidPoseOnDeviceModel = new RigidPoseOnDeviceModel(
                            onDeviceModel.getModelPath(),
                            onDeviceModel.getModelId(),
                            onDeviceModel.getModelVersion(),
                            managedModel
                    );
                    FritzVisionRigidPosePredictor predictor = new FritzVisionRigidPosePredictor(rigidPoseOnDeviceModel, options);
                    statusListener.onPredictorReady(predictor);
                }
            }, useWifi);
        }
    }

}

