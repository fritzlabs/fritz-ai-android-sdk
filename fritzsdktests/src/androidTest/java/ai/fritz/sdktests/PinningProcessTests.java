package ai.fritz.sdktests;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;

import androidx.test.runner.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ai.fritz.core.Fritz;
import ai.fritz.core.FritzManagedModel;
import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.core.ModelReadyListener;
import ai.fritz.core.utils.FritzModelManager;
import ai.fritz.core.utils.JobUtil;
import ai.fritz.core.utils.PreferenceManager;
import ai.fritz.sdktests.BaseFritzTest;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class PinningProcessTests extends BaseFritzTest {
    private static final int TIMEOUT_SECONDS = 10;
    private static final String modelId = UUID.randomUUID().toString();
    private static final String modelPath = "mnist.tflite";
    private static final String pinKey = "model_pinned_version";

    @Before
    public void setup() {
        super.setup();

        // Clear the key for this model and initialize
        PreferenceManager.clearAll(appContext);
        Fritz.configure(appContext);
    }

    /**
     * Ensuring that a request for a new model is not made if the desired model versions are equal
     */
    @Test
    public void testLoadUnpinnedModel() {
        FritzOnDeviceModel model = new FritzOnDeviceModel(modelPath, modelId, 1);
        PreferenceManager.saveModel(appContext, model);

        Integer[] foundModel = new Integer[1];
        FritzModelManager manager = new FritzModelManager(model);

        final CountDownLatch latch = new CountDownLatch(1);
        ModelReadyListener listener = new ModelReadyListener() {
            @Override
            public void onModelReady(FritzOnDeviceModel onDeviceModel) {
                foundModel[0] = onDeviceModel.getPinnedVersion();
                latch.countDown();
            }
        };

        // Attempt to check and download the latest model version
        manager.loadModel(listener);

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail();
        }

        // The same model is being requested, so the version is unchanged
        assertNull(foundModel[0]);
    }

    /**
     * Ensuring that a job containing pinned information is correctly posted.
     */
    @Test
    public void testCheckPinSuccess() {
        FritzOnDeviceModel model = new FritzOnDeviceModel(modelPath, modelId, 2, 2);

        // Check for an update
        JobUtil.checkForModelUpdate(appContext, model, true);
        JobScheduler jobScheduler = (JobScheduler) appContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> jobList = jobScheduler.getAllPendingJobs();

        // The correct pinned version is found in the package
        for (JobInfo job : jobList) {
            String managedModelSerialized = job.getExtras().getString(FritzManagedModel.MANAGED_MODEL_KEY);
            FritzManagedModel managedModel = FritzManagedModel.extractFromString(managedModelSerialized);
            if(managedModel.getModelId().equals(model.getModelId())) {
                assertEquals(2, managedModel.getPinnedVersion().intValue());
            }
        }
    }

    /**
     * Ensuring that the pinned status of a model is correctly saved.
     */
    @Test
    public void testRestorePinnedModel() {
        // Saving an unpinned model
        FritzOnDeviceModel model = new FritzOnDeviceModel(modelPath, modelId, 3);
        PreferenceManager.saveModel(appContext, model);

        FritzOnDeviceModel onDeviceModel = PreferenceManager.getSavedModel(appContext, modelId);
        JSONObject modelJson = onDeviceModel.toJson();

        // Restored model does not have a pin key since it was unpinned
        assertFalse(modelJson.has(pinKey));

        // Pin a version
        model.setPinnedVersion(2);

        PreferenceManager.saveModel(appContext, model);
        FritzOnDeviceModel onDeviceModel2 = PreferenceManager.getSavedModel(appContext, modelId);
        JSONObject modelJson2 = onDeviceModel2.toJson();

        // Restored model has the assigned pinned version
        assertTrue(modelJson2.has(pinKey));
        try {
            assertEquals(modelJson2.getInt(pinKey), 2);
        } catch (JSONException e) {
            fail("Pinned version not found.");
        }
    }
}
