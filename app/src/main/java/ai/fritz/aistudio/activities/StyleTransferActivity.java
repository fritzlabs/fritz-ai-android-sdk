package ai.fritz.aistudio.activities;

import android.graphics.Bitmap;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.util.Size;

import ai.fritz.aistudio.R;
import ai.fritz.aistudio.activities.base.BaseRecordingActivity;
import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.styletransfer.FritzVisionStylePredictor;
import ai.fritz.vision.styletransfer.FritzVisionStylePredictorOptions;
import ai.fritz.vision.styletransfer.FritzVisionStyleResult;
import android.util.Log;


public class StyleTransferActivity extends BaseRecordingActivity implements OnImageAvailableListener {
    private FritzVisionStylePredictor predictor;
    private FritzOnDeviceModel[] styles;

    @Override
    protected int getModelOptionsTextId() {
        return R.array.style_transfer_options;
    }

    @Override
    protected synchronized Bitmap runPrediction(FritzVisionImage visionImage, Size cameraViewSize) {
        if (predictor != null) {
            FritzVisionStyleResult styleResult = predictor.predict(visionImage);
            return styleResult.toBitmap();
        }
        return visionImage.buildOrientedBitmap();
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        if (predictor != null) {
            predictor.close();
            predictor = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        styles = FritzVisionModels.getPaintingStyleModels().getAll();
    }

    @Override
    protected void loadPredictor(int choice) {
        FritzOnDeviceModel onDeviceModel = getModel(choice);
        FritzVisionStylePredictorOptions options = new FritzVisionStylePredictorOptions();
        predictor = FritzVision.StyleTransfer.getPredictor(onDeviceModel, options);
    }

    private FritzOnDeviceModel getModel(int choice) {
        return styles[choice];
    }
}

