package ai.fritz.aistudio.activities;

import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.View;

import androidx.arch.core.util.Function;

import java.util.List;

import ai.fritz.aistudio.R;
import ai.fritz.aistudio.activities.base.BaseLiveVideoActivity;
import ai.fritz.aistudio.ui.OptionMenu;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.ModelVariant;
import ai.fritz.vision.filter.OneEuroFilterMethod;
import ai.fritz.vision.poseestimation.FritzVisionPosePredictor;
import ai.fritz.vision.poseestimation.FritzVisionPosePredictorOptions;
import ai.fritz.vision.poseestimation.FritzVisionPoseResult;
import ai.fritz.vision.poseestimation.Pose;
import ai.fritz.vision.poseestimation.PoseOnDeviceModel;


public class PoseEstimationActivity extends BaseLiveVideoActivity {
    private static final int MAX_POSES_TO_DETECT = 5;

    private FritzVisionPosePredictor posePredictor;
    private FritzVisionPoseResult poseResult;
    private FritzVisionPosePredictorOptions options;
    private PoseOnDeviceModel onDeviceModel;
    private final boolean RUN_ON_GPU = true;

    private OptionMenu bottomSheet;

    @Override
    public void onPreviewSizeChosen(final Size size, final Size cameraSize) {
        super.onPreviewSizeChosen(size, cameraSize);
        onDeviceModel = FritzVisionModels.getHumanPoseEstimationOnDeviceModel(ModelVariant.FAST);
        options = new FritzVisionPosePredictorOptions();
        options.useGPU = RUN_ON_GPU;
        options.smoothingOptions = new OneEuroFilterMethod();

        if (bottomSheet == null) {
            initOptionsMenu();
        }
    }

    @Override
    protected void onCameraSetup(final Size cameraSize) {
        if (!RUN_ON_GPU) {
            posePredictor = FritzVision.PoseEstimation.getPredictor(onDeviceModel, options);
        }
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        if (posePredictor != null) {
            posePredictor.close();
        }
    }

    @Override
    protected void handleDrawingResult(Canvas canvas, Size cameraSize) {
        if (poseResult != null) {
            List<Pose> poses = poseResult.getPoses();
            for (Pose pose : poses) {
                pose.draw(canvas);
            }
        }
    }

    @Override
    protected void runInference(FritzVisionImage fritzVisionImage) {
        if (RUN_ON_GPU && posePredictor == null) {
            posePredictor = FritzVision.PoseEstimation.getPredictor(onDeviceModel, options);
        }
        poseResult = posePredictor.predict(fritzVisionImage);
    }

    /**
     * Sets the functionality of the option menu
     */
    private void initOptionsMenu() {
        final Function<Float, Void> changeMinPose = new Function<Float, Void>() {
            @Override
            public Void apply(Float input) {
                options.minPoseThreshold = input;
                return null;
            }
        };

        final Function<Float, Void> changeMinPart = new Function<Float, Void>() {
            @Override
            public Void apply(Float input) {
                options.minPartThreshold = input;
                return null;
            }
        };

        final Function<Float, Void> changeNumPose = new Function<Float, Void>() {
            @Override
            public Void apply(Float input) {
                options.maxPosesToDetect = Math.round(input);
                return null;
            }
        };

        bottomSheet = findViewById(R.id.option_menu);
        bottomSheet.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                bottomSheet
                        .withSlider("Min Pose Threshold", 1, options.minPoseThreshold, changeMinPose)
                        .withSlider("Min Part Threshold", 1, options.minPartThreshold, changeMinPart)
                        .withSlider("Number of Poses", MAX_POSES_TO_DETECT, options.maxPosesToDetect, changeNumPose, 1f);
            }
        });
    }
}
