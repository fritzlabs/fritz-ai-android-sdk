package ai.fritz.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.UUID;

import ai.fritz.core.api.ApiClient;
import ai.fritz.core.api.Session;
import ai.fritz.core.constants.SPKeys;
import ai.fritz.core.testutils.TestDataFactory;
import ai.fritz.core.utils.FritzModelManager;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 27, packageName = "ai.fritz.sdkapp")
public class FritzModelManagerTest extends BaseUnitTest {
    private static final String MODEL_PATH_INCLUDED = "file:///android_asset/mobilenet_v2_1_0_224_frozen.pb";

    @Before
    public void setup() {
        super.setup();
        Session session = Fritz.intializeSession(context, TEST_API_KEY);
        ApiClient apiClient = mock(ApiClient.class);
        SessionManager sessionContext = new SessionManager(context.getApplicationContext(), session, apiClient);
        Fritz.configure(sessionContext);
    }

    /**
     * Test if no active model version exists. Active custom model will be the one passed in.
     */
    @Test
    public void testHandleModelInitialized() throws Exception {
        String modelId = UUID.randomUUID().toString();
        FritzOnDeviceModel testOnDeviceModel = TestDataFactory.createCustomModel(MODEL_PATH_INCLUDED, modelId, 1);

        // No existing model
        String key = SPKeys.getModelKeyById(modelId);
        when(sharedPrefs.getString(key, null)).thenReturn(null);

        // Initialize without existing state
        FritzModelManager.handleModelInitialized(testOnDeviceModel);

        // Verify that the model was saved
        verify(editor, times(1)).putString(eq(key), eq(testOnDeviceModel.toJson().toString()));

        // Verify that the model version install event was sent
        String modelVersionInstall = SPKeys.getHasTrackedModelVersionKey(testOnDeviceModel.getModelId(), testOnDeviceModel.getModelVersion());
        verify(editor, times(1)).putBoolean(eq(modelVersionInstall), eq(true));
    }

    @Test
    public void testInstallEventNotTriggered() {
        String modelId = UUID.randomUUID().toString();
        FritzOnDeviceModel testOnDeviceModel = TestDataFactory.createCustomModel(MODEL_PATH_INCLUDED, modelId, 1);

        // Set up the shared preferences to keep a state that the model has already been installed.
        String modelVersionInstall = SPKeys.getHasTrackedModelVersionKey(testOnDeviceModel.getModelId(), testOnDeviceModel.getModelVersion());
        when(sharedPrefs.getBoolean(modelVersionInstall, false)).thenReturn(true);

        // Simulate initializing the model again (for whatever reason)
        FritzModelManager.handleModelInitialized(testOnDeviceModel);
        // Verify that the install event wasn't called
        verify(editor, never()).putBoolean(eq(modelVersionInstall), eq(true));
    }
}
