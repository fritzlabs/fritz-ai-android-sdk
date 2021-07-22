package ai.fritz.sdktests.styletransfer;

import ai.fritz.core.FritzOnDeviceModel;

public class CustomStyleTransferModel extends FritzOnDeviceModel {
    private static final String MODEL_PATH = "file:///android_asset/starry_night_200x100_025.tflite";
    private static final String MODEL_ID = "791304461a6d4b398061a8b391460988";

    public CustomStyleTransferModel() {
        super(MODEL_PATH, MODEL_ID, 1);
    }

}
