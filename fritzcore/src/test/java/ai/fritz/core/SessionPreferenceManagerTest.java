package ai.fritz.core;


import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import ai.fritz.core.api.Session;
import ai.fritz.core.api.SessionSettings;
import ai.fritz.core.constants.SPKeys;
import ai.fritz.core.utils.SessionPreferenceManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 27, packageName = "ai.fritz.sdkapp")
public class SessionPreferenceManagerTest extends BaseUnitTest {

    private static final String TEST_INSTANCE_ID = "test-instance-123456";
    private static final String TEST_APP_TOKEN = "app-token-123456";
    private static final String TEST_USER_AGENT = "some-user-agent";

    @Test
    public void testCreateSession() {
        // Test that we create the session properly.
        Session expectedSession = new Session(TEST_INSTANCE_ID, TEST_APP_TOKEN, TEST_USER_AGENT);
        Session actualSession = SessionPreferenceManager.createSession(context, TEST_INSTANCE_ID, TEST_APP_TOKEN, TEST_USER_AGENT);
        assertEquals(actualSession, expectedSession);
    }

    @Test
    public void testUpdateSessionSettings() throws JSONException {
        Session expectedSession = new Session(TEST_INSTANCE_ID, TEST_APP_TOKEN, TEST_USER_AGENT);
        when(sharedPrefs.getString(SPKeys.FRITZ_SESSION, null)).thenReturn(expectedSession.toJson().toString());

        // Created a new session settings
        SessionSettings updatedSettings = SessionSettings.createDefault();
        updatedSettings.setSettingsLastCheckedAt(System.currentTimeMillis());

        // update the current session
        Session actualSession = SessionPreferenceManager.updateSessionSettings(context, updatedSettings);

        // check that the updated settings includes the new value
        expectedSession.setSettings(updatedSettings);
        assertEquals(actualSession, expectedSession);
    }

    @Test
    public void testGetSession() throws JSONException {
        // Test that we fetch the session from storage
        Session expectedSession = new Session(TEST_INSTANCE_ID, TEST_APP_TOKEN, TEST_USER_AGENT);
        when(sharedPrefs.getString(SPKeys.FRITZ_SESSION, null)).thenReturn(expectedSession.toJson().toString());
        Session actualSession = SessionPreferenceManager.getSession(context);
        assertEquals(actualSession, expectedSession);
    }
}
