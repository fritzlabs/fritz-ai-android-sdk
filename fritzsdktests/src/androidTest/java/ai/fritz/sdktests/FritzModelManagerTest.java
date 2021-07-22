package ai.fritz.sdktests;

import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.core.utils.FritzModelManager;
import ai.fritz.core.utils.PreferenceManager;
import ai.fritz.sdktests.BaseFritzTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class FritzModelManagerTest extends BaseFritzTest {

    private static final String MODEL_ID = UUID.randomUUID().toString();

    // This model actually exists
    private static final String MODEL_PATH = "file:///android_asset/dentastix_chris_260x200_35_small_1560877588.tflite";

    @After
    public void tearDown() {
        PreferenceManager.clearAll(appContext);
    }

    @Test
    public void testModelInitialized() {
        FritzOnDeviceModel onDeviceModel = new FritzOnDeviceModel(MODEL_PATH, MODEL_ID, 1);
        FritzModelManager.handleModelInitialized(onDeviceModel);
        FritzOnDeviceModel savedModel = PreferenceManager.getSavedModel(appContext, onDeviceModel.getModelId());
        assertEquals(onDeviceModel, savedModel);
    }

    @Test
    public void testExistingModelDeleted() {
        // Setup Behavior: Developer deleted the model that was saved.
        FritzOnDeviceModel deletedOnDeviceModel = new FritzOnDeviceModel("file:///android_asset/dentastix_model_not_exist.tflite", MODEL_ID, 1);
        PreferenceManager.saveModel(appContext, deletedOnDeviceModel);

        // Updated model version with existing model path
        FritzOnDeviceModel includedOnDeviceModel = new FritzOnDeviceModel(MODEL_PATH, MODEL_ID, 2);
        FritzModelManager.handleModelInitialized(includedOnDeviceModel);

        // Verify that the saved model was updated
        FritzOnDeviceModel savedModel = PreferenceManager.getSavedModel(appContext, includedOnDeviceModel.getModelId());
        assertEquals(includedOnDeviceModel, savedModel);
    }
}
