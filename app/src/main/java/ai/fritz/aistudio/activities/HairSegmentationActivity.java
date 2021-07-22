package ai.fritz.aistudio.activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.util.Size;

import ai.fritz.aistudio.R;
import ai.fritz.aistudio.activities.base.BaseLiveGPUActivity;
import ai.fritz.aistudio.ui.ColorSlider;
import ai.fritz.aistudio.ui.OptionMenu;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.ModelVariant;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.imagesegmentation.BlendMode;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictor;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictorOptions;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationResult;
import ai.fritz.vision.imagesegmentation.SegmentationOnDeviceModel;
import ai.fritz.vision.imagesegmentation.MaskClass;

import androidx.arch.core.util.Function;


public class HairSegmentationActivity extends BaseLiveGPUActivity {
    private int maskColor = Color.RED;
    private float hairConfidenceThreshold = .7f;
    private int hairAlpha = 180;
    private final int ALPHA_MAX = 255;
    private static final BlendMode BLEND_MODE = BlendMode.SOFT_LIGHT;
    private static final boolean RUN_ON_GPU = true;

    private FritzVisionSegmentationPredictor hairPredictor;
    private FritzVisionSegmentationResult hairResult;
    private FritzVisionSegmentationPredictorOptions options;

    private ColorSlider colorPicker;
    private OptionMenu bottomSheet;

    private SegmentationOnDeviceModel onDeviceModel;

    public void onCreate(final Bundle savedInstanceState) {
        setCameraFacingDirection(CameraCharacteristics.LENS_FACING_FRONT);
        super.onCreate(savedInstanceState);

        // Create the segmentation options.
        options = new FritzVisionSegmentationPredictorOptions();
        options.confidenceThreshold = hairConfidenceThreshold;
        options.useGPU = RUN_ON_GPU;

        // Set the on device model
        onDeviceModel = FritzVisionModels.getHairSegmentationOnDeviceModel(ModelVariant.FAST);

        if (!RUN_ON_GPU) {
            // Load the predictor when the activity is created (iff not running on the GPU)
            hairPredictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel, options);
        }
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final Size cameraSize) {
        super.onPreviewSizeChosen(size, cameraSize);

        // Adds the option menu to the view
        if (bottomSheet == null) {
            initOptionsMenu();
        }

        // Adds the color slider to the view
        if (colorPicker == null) {
            colorPicker = findViewById(R.id.color_picker);

            // Change the mask color upon using the slider
            colorPicker.setOnColorChangeListener(new ColorSlider.OnColorChangeListener() {
                @Override
                public void onColorChange(int selectedColor) {
                    if (selectedColor != Color.TRANSPARENT) {
                        maskColor = selectedColor;
                    }
                }
            });
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_color_slider;
    }

    @Override
    protected void runInference(FritzVisionImage fritzVisionImage) {
        if (RUN_ON_GPU && hairPredictor == null) {
            hairPredictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel, options);
        }
        hairResult = hairPredictor.predict(fritzVisionImage);
        Bitmap alphaMask = hairResult.buildSingleClassMask(MaskClass.HAIR, hairAlpha, 1f, options.confidenceThreshold, maskColor);
        fritzSurfaceView.drawBlendedMask(fritzVisionImage, alphaMask, BLEND_MODE, getCameraFacingDirection() == CameraCharacteristics.LENS_FACING_FRONT);
    }

    /**
     * Sets the functionality of the option menu
     */
    private void initOptionsMenu() {
        final Function<Float, Void> changeAlpha = new Function<Float, Void>() {
            @Override
            public Void apply(Float input) {
                hairAlpha = Math.round(input);
                return null;
            }
        };

        final Function<Float, Void> changeMaxConfidence = new Function<Float, Void>() {
            @Override
            public Void apply(Float input) {
                options.confidenceThreshold = input;
                return null;
            }
        };

        bottomSheet = findViewById(R.id.option_menu);
        bottomSheet
                .withSlider("Alpha", ALPHA_MAX, hairAlpha, changeAlpha, 1f)
                .withSlider("Confidence Threshold", 1, hairConfidenceThreshold, changeMaxConfidence, 0.01f);
    }
}
