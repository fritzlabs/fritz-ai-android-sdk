package ai.fritz.sdktests;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;

import ai.fritz.core.Fritz;
import ai.fritz.vision.base.FritzVisionPredictor;

public class BaseFritzTest<T extends FritzVisionPredictor> {

    protected static final int TIMEOUT_SECONDS = 10;

    private static final String TAG = BaseFritzTest.class.getSimpleName();

    protected Context appContext;
    protected Context testContext;

    @Before
    public void setup() {
        // Context of the app under test.
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        testContext = InstrumentationRegistry.getInstrumentation().getContext();

        Fritz.configure(appContext, "4c6e348aedaa40e48b46b0a19ef4e5dd");
    }
}
