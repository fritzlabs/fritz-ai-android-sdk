package ai.fritz.aistudio.activities;

import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.View;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.fritz.aistudio.R;
import ai.fritz.aistudio.activities.base.BaseCameraActivity;
import ai.fritz.aistudio.ui.OptionMenu;
import ai.fritz.aistudio.ui.ResultsView;
import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionOrientation;
import ai.fritz.vision.ImageOrientation;
import ai.fritz.vision.ImageRotation;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.imagelabeling.FritzVisionLabelPredictor;
import ai.fritz.vision.imagelabeling.FritzVisionLabelPredictorOptions;
import ai.fritz.vision.imagelabeling.FritzVisionLabelResult;
import ai.fritz.vision.imagelabeling.LabelingOnDeviceModel;

import androidx.arch.core.util.Function;


public class ImageLabelingActivity extends BaseCameraActivity implements ImageReader.OnImageAvailableListener {
    private static final String TAG = ImageLabelingActivity.class.getSimpleName();

    private AtomicBoolean computing = new AtomicBoolean(false);

    private FritzVisionLabelPredictor predictor;
    private FritzVisionLabelPredictorOptions options;
    private ImageOrientation orientation;

    ResultsView resultsView;

    private OptionMenu bottomSheet;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.image_labeling_title);

        options = new FritzVisionLabelPredictorOptions();
        options.useGPU = true;
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        if(predictor != null) {
            predictor.close();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment;
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final Size cameraSize) {
        orientation = FritzVisionOrientation.getImageOrientationFromCamera(this, cameraId);
        LabelingOnDeviceModel labelingOnDeviceModel = FritzVisionModels.getImageLabelingOnDeviceModel();
        predictor = FritzVision.ImageLabeling.getPredictor(labelingOnDeviceModel, options);

        if (resultsView == null) {
            resultsView = findViewById(R.id.results);
        }

        if (bottomSheet == null) {
            initOptionsMenu();
        }
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        Image image = reader.acquireLatestImage();

        if (image == null) {
            return;
        }

        if (!computing.compareAndSet(false, true)) {
            image.close();
            return;
        }

        final FritzVisionImage fritzImage = FritzVisionImage.fromMediaImage(image, orientation);
        image.close();

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        FritzVisionLabelResult labels = predictor.predict(fritzImage);
                        resultsView.setResult(labels.getVisionLabels());
                        fritzImage.release();
                        requestRender();
                        computing.set(false);
                    }
                });
    }

    /**
     * Sets the functionality of the option menu
     */
    private void initOptionsMenu() {
        final Function<Float, Void> changeMaxConfidence = new Function<Float, Void>() {
            @Override
            public Void apply(Float input) {
                options.confidenceThreshold = input;
                return null;
            }
        };

        bottomSheet = findViewById(R.id.option_menu);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                bottomSheet
                        .withHeader((View) resultsView)
                        .withSlider("Confidence Threshold", 1, options.confidenceThreshold, changeMaxConfidence);
            }
        });
    }
}