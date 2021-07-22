package ai.fritz.aistudio.activities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Size;
import android.view.View;

import androidx.arch.core.util.Function;

import ai.fritz.aistudio.R;
import ai.fritz.aistudio.activities.base.BaseLiveVideoActivity;
import ai.fritz.aistudio.ui.OptionMenu;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.ModelVariant;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictor;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictorOptions;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationResult;
import ai.fritz.vision.imagesegmentation.MaskClass;
import ai.fritz.vision.imagesegmentation.SegmentationOnDeviceModel;


public class PetSegmentationActivity extends BaseLiveVideoActivity {
    private final int ALPHA_MAX = 255;
    private int petAlpha = 180;

    private FritzVisionSegmentationPredictor petPredictor;
    private FritzVisionSegmentationResult petResult;
    private FritzVisionSegmentationPredictorOptions options;

    private OptionMenu bottomSheet;

    @Override
    public void onPreviewSizeChosen(final Size size, final Size cameraSize) {
        super.onPreviewSizeChosen(size, cameraSize);

        options = new FritzVisionSegmentationPredictorOptions();
        options.confidenceThreshold = .4f;
        options.useGPU = true;

        if (bottomSheet == null) {
            initOptionsMenu();
        }
    }

    @Override
    protected void onCameraSetup(final Size cameraSize) {
        SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getPetSegmentationOnDeviceModel(ModelVariant.FAST);
        petPredictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel, options);
    }

    @Override
    protected void handleDrawingResult(Canvas canvas, Size cameraSize) {
        if (petResult != null) {
            Bitmap maskBitmap = petResult.buildSingleClassMask(MaskClass.PET, petAlpha, 1, options.confidenceThreshold);
            canvas.drawBitmap(maskBitmap, null, new RectF(0, 0, cameraSize.getWidth(), cameraSize.getHeight()), null);
        }
    }

    @Override
    protected void runInference(FritzVisionImage fritzVisionImage) {
        petResult = petPredictor.predict(fritzVisionImage);
    }

    /**
     * Sets the functionality of the option menu
     */
    private void initOptionsMenu() {
        final Function<Float, Void> changeAlpha = new Function<Float, Void>() {
            @Override
            public Void apply(Float input) {
                petAlpha = Math.round(input);
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
        bottomSheet.setVisibility(View.VISIBLE);
        bottomSheet
                .withSlider("Alpha", ALPHA_MAX, petAlpha, changeAlpha, 1f)
                .withSlider("Confidence Threshold", 1, options.confidenceThreshold, changeMaxConfidence);
    }
}
