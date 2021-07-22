package ai.fritz.aistudio.activities.base;

import android.graphics.Canvas;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.fritz.aistudio.R;
import ai.fritz.aistudio.ui.OverlayView;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionOrientation;
import ai.fritz.vision.ImageOrientation;

public abstract class BaseLiveVideoActivity extends BaseCameraActivity implements ImageReader.OnImageAvailableListener {
    private static final String TAG = BaseLiveVideoActivity.class.getSimpleName();
    private AtomicBoolean computing = new AtomicBoolean(false);

    private ImageOrientation imageOrientation;
    protected Button chooseModelBtn;
    protected ImageButton cameraSwitchBtn;
    protected FritzVisionImage fritzVisionImage;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final Size cameraSize) {
        chooseModelBtn = findViewById(R.id.choose_model_btn);
        cameraSwitchBtn = findViewById(R.id.camera_switch_btn);

        imageOrientation = FritzVisionOrientation.getImageOrientationFromCamera(this, cameraId);

        setCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        handleDrawingResult(canvas, cameraSize);
                        computing.set(false);
                    }
                });

        cameraSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCameraFacingDirection();
            }
        });

        runInBackground(new Runnable() {
            @Override
            public void run() {
                onCameraSetup(cameraSize);
            }
        });
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        final Image image = reader.acquireLatestImage();

        if (image == null) {
            return;
        }

        if (!computing.compareAndSet(false, true)) {
            image.close();
            return;
        }
        fritzVisionImage = FritzVisionImage.fromMediaImage(image, imageOrientation);
        image.close();

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        runInference(fritzVisionImage);
                        requestRender();
                    }
                });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment_tracking;
    }

    @Override
    public void onSetDebug(final boolean debug) {

    }

    protected abstract void onCameraSetup(Size cameraSize);

    protected abstract void handleDrawingResult(Canvas canvas, Size cameraSize);

    protected abstract void runInference(FritzVisionImage fritzVisionImage);
}
