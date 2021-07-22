package ai.fritz.sdktests;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ai.fritz.core.Fritz;
import ai.fritz.core.utils.PreferenceManager;
import ai.fritz.sdktests.BaseFritzTest;
import ai.fritz.sdktests.TestingAsset;
import ai.fritz.sdktests.TestingAssetHelper;
import ai.fritz.sdktests.styletransfer.CustomStyleTransferManagedModel;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.PredictorStatusListener;
import ai.fritz.vision.styletransfer.FritzVisionStylePredictor;
import ai.fritz.vision.styletransfer.FritzVisionStylePredictorOptions;
import ai.fritz.vision.styletransfer.FritzVisionStyleResult;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class OTAModelUpdateTest extends BaseFritzTest {


    private FritzVisionStylePredictor stylePredictor;
    private CustomStyleTransferManagedModel managedModel;

    @Before
    public void setup() {
        super.setup();
        managedModel = new CustomStyleTransferManagedModel();

        // Clear the key for this model and initialize
        PreferenceManager.clearAll(appContext);
        Fritz.configure(appContext);

        // Make sure this file is deleted
        String savedFile = managedModel.getModelId() + "_v1.tflite";
        File modelFile = new File(appContext.getFilesDir(), savedFile);
        modelFile.delete();
    }

    @Test
    public void testOTAUpdate() {

        // Check to make sure that the model downloads
        final CountDownLatch latch = new CountDownLatch(1);
        FritzVision.StyleTransfer.loadPredictor(managedModel, new PredictorStatusListener<FritzVisionStylePredictor>() {
            @Override
            public void onPredictorReady(FritzVisionStylePredictor stylePredictor) {
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail();
        }
    }

    // This shouldn't work. Please fix in https://fritzlabs.atlassian.net/browse/SDK-524
    // and then fix this test.
    @Test
    @Ignore
    public void testOTAWithGPUOption() {
        // If you're using the GPU, this must run on the same thread.
        CustomStyleTransferManagedModel managedModel = new CustomStyleTransferManagedModel();
        final CountDownLatch latch = new CountDownLatch(1);
        FritzVisionStylePredictorOptions options = new FritzVisionStylePredictorOptions();

        HandlerThread testThread = new HandlerThread("test");
        testThread.start();
        Handler handler = new Handler(testThread.getLooper());

        final CountDownLatch loadLatch = new CountDownLatch(1);

        handler.post(new Runnable() {
            @Override
            public void run() {

                FritzVision.StyleTransfer.loadPredictor(managedModel, options, new PredictorStatusListener<FritzVisionStylePredictor>() {
                    @Override
                    public void onPredictorReady(FritzVisionStylePredictor predictor) {
                        stylePredictor = predictor;
                        loadLatch.countDown();
                    }
                });
            }
        });

        try {
            loadLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail();
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.LIVING_ROOM);
                FritzVisionStyleResult styleResult = stylePredictor.predict(testImage);
                assertNotNull(styleResult);
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            testThread.quit();
        } catch (InterruptedException e) {
            fail();
        }
    }
}