package ai.fritz.sdktests;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ai.fritz.core.api.FatalErrorListener;
import ai.fritz.core.api.GetRequestTask;
import ai.fritz.core.api.Request;
import ai.fritz.core.api.RequestHandler;
import ai.fritz.core.api.Session;
import ai.fritz.core.utils.PreferenceManager;
import ai.fritz.core.utils.SessionPreferenceManager;
import ai.fritz.core.utils.UserAgentUtil;

import static org.junit.Assert.fail;

@Ignore("Skipping test that involve the Fritz AI backend.")
@RunWith(AndroidJUnit4.class)
public class SDKAuthenticateTest {

    private static final String TEST_INVALID_APP_TOKEN = "invalid-app-testing-token";
    private static final int TIMEOUT_SECONDS = 10;

    Context appContext;

    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getTargetContext();
    }

    @After
    public void tearDown() {
        PreferenceManager.clearAll(appContext);
    }

    /**
     * This test makes sure that if the API Key is invalid, the fatal error listener picks it up.
     */
    @Test
    public void testSDKNotAuthenticated() {
        String instanceId = UUID.randomUUID().toString();

        PackageManager packageManager = appContext.getPackageManager();
        // Setup the app token. Look for it in the AndroidManifest first.
        String packageName = null;
        String versionName = null;
        String appName = null;
        int versionCode = 0;
        try {
            ApplicationInfo ai = packageManager.getApplicationInfo(appContext.getPackageName(), PackageManager.GET_META_DATA);
            PackageInfo pInfo = packageManager.getPackageInfo(appContext.getPackageName(), 0);
            appName = (String) packageManager.getApplicationLabel(ai);

            versionName = pInfo.versionName;
            versionCode = pInfo.versionCode;
            packageName = pInfo.packageName;

        } catch (PackageManager.NameNotFoundException e) {
            fail();
        }

        String userAgent = UserAgentUtil.create(appName, packageName, versionName, versionCode);
        Session session = SessionPreferenceManager.createSession(appContext, instanceId, TEST_INVALID_APP_TOKEN, userAgent);

        String apiBase = appContext.getString(ai.fritz.core.R.string.api_base) + "/sdk/v1";

        final CountDownLatch latch = new CountDownLatch(1);
        try {

            URL url = new URL(apiBase + "/session/settings");
            RequestHandler handler = new RequestHandler() {
                @Override
                public void onSuccess(JSONObject response) {
                    fail();
                }

                @Override
                public void onError(JSONObject response) {
                }
            };
            FatalErrorListener listener = new FatalErrorListener() {
                @Override
                public void onFatalError(String message) {
                    Assert.assertTrue(message.contains(TEST_INVALID_APP_TOKEN));
                    latch.countDown();
                }
            };

            new GetRequestTask(session, handler, listener).execute(new Request(url));
        } catch (MalformedURLException e) {
            fail();
        }

        // Wait until the latch reaches 0 to exit the test.
        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail();
        }
    }
}