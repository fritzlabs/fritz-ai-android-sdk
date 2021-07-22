package ai.fritz.aistudio.activities;

import android.graphics.Canvas;
import android.util.Size;

import java.util.List;

import ai.fritz.aistudio.activities.base.BaseLiveVideoActivity;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.FritzVisionObject;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictor;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictorOptions;
import ai.fritz.vision.objectdetection.FritzVisionObjectResult;
import ai.fritz.vision.objectdetection.ObjectDetectionOnDeviceModel;


public class ObjectDetectionActivity extends BaseLiveVideoActivity {

    private FritzVisionObjectPredictor objectPredictor;
    private FritzVisionObjectResult objectResult;

    @Override
    protected void onCameraSetup(final Size cameraSize) {
        ObjectDetectionOnDeviceModel onDeviceModel = FritzVisionModels.getObjectDetectionOnDeviceModel();
        FritzVisionObjectPredictorOptions options = new FritzVisionObjectPredictorOptions();
        options.useGPU = true;
        options.confidenceThreshold = 0.6f;
        objectPredictor = FritzVision.ObjectDetection.getPredictor(onDeviceModel, options);
    }

    @Override
    protected void handleDrawingResult(Canvas canvas, Size cameraSize) {
        if (objectResult != null) {
            List<FritzVisionObject> detections = objectResult.getObjects();
            for (FritzVisionObject object : detections) {
                object.draw(canvas);
            }
        }
    }

    @Override
    protected void runInference(FritzVisionImage fritzVisionImage) {
        objectResult = objectPredictor.predict(fritzVisionImage);
    }
}
