package ai.fritz.aistudio.ml;

import android.content.Context;

import ai.fritz.aistudio.R;
import ai.fritz.core.FritzOnDeviceModel;

public class MnistFritzOnDeviceModel extends FritzOnDeviceModel {
    private static final String MODEL_PATH = "file:///android_asset/mnist.tflite";
    private static final int MODEL_VERSION = 1;

    public MnistFritzOnDeviceModel(Context context) {
        super(MODEL_PATH, context.getString(R.string.tflite_model_id), MODEL_VERSION);
    }
}

