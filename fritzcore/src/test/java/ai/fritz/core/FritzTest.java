package ai.fritz.core;

import android.content.res.Resources;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.UUID;

import ai.fritz.core.api.Session;
import ai.fritz.core.constants.SPKeys;
import ai.fritz.core.utils.UserAgentUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 27, packageName = "ai.fritz.sdkapp")
public class FritzTest extends BaseUnitTest {

    private static String API_KEY = "fritz_api_key";
    private static String TEST_INSTANCE_ID = UUID.randomUUID().toString();

    /**
     * Configure when the instance id still needs to be created.
     */
    @Test
    public void testNewConfigure() {
        configureFritz(TEST_API_KEY);

        // Capture the saved session
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(editor).putString(eq(SPKeys.FRITZ_SESSION), argument.capture());
        try {
            // Convert the captured string to a session
            Session actualSession = new Session(new JSONObject(argument.getValue()));

            // Expected user agent string.
            String expectedUserAgent = UserAgentUtil.create(APP_LABEL, packageInfo.packageName, packageInfo.versionName, packageInfo.versionCode);

            // Check the saved session attributes are what we expect.
            assertEquals(TEST_API_KEY, actualSession.getAppToken());
            assertNotNull(actualSession.getInstanceId());
            assertEquals(expectedUserAgent, actualSession.getUserAgent());
        } catch (JSONException e) {
            fail();
        }
    }

    @Test
    public void testNullMetadata() {
        Resources resources = mock(Resources.class);

        // Set the metadata as null
        applicationInfo.metaData = null;

        // Any number to simulate a resource id.
        int testResourceId = 6;
        when(resources.getIdentifier(API_KEY, "string", PACKAGE_NAME)).thenReturn(testResourceId);
        when(context.getResources()).thenReturn(resources);
        doReturn(TEST_API_KEY).when(context).getString(eq(testResourceId));

        // Call configure
        configureFritz(null);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(editor).putString(eq(SPKeys.FRITZ_SESSION), argument.capture());
        try {
            // Convert the captured string to a session
            Session actualSession = new Session(new JSONObject(argument.getValue()));

            // Check the API key was saved
            assertEquals(TEST_API_KEY, actualSession.getAppToken());
        } catch (JSONException e) {
            fail();
        }
    }

    /**
     * Configure when the instance id is already saved.
     */
    @Test
    public void testExistingInstanceConfigure() {
        String expectedUserAgent = UserAgentUtil.create(APP_LABEL, packageInfo.packageName, packageInfo.versionName, packageInfo.versionCode);
        Session existingSession = new Session(TEST_INSTANCE_ID, TEST_API_KEY, expectedUserAgent);
        try {
            // Return this existing session
            when(sharedPrefs.getString(SPKeys.FRITZ_SESSION, null)).thenReturn(existingSession.toJson().toString());

            // Configure the session
            Session session = configureFritz(TEST_API_KEY);

            // Capture the saved session
            ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
            verify(editor).putString(eq(SPKeys.FRITZ_SESSION), argument.capture());

            // Convert the captured string to a session
            Session actualSession = new Session(new JSONObject(argument.getValue()));

            // Check the saved session attributes are what we expect.
            assertEquals(TEST_API_KEY, actualSession.getAppToken());
            assertEquals(session.getInstanceId(), actualSession.getInstanceId());
            assertEquals(expectedUserAgent, actualSession.getUserAgent());
        } catch (JSONException e) {
            fail();
        }
    }

    /**
     * Test out fetching the api key from the metadata
     */
    @Test
    public void testConfigureWithApiKeyInMetadata() {
        // Set the api key up in the metadata bundle
        Bundle metadataBundle = new Bundle();
        metadataBundle.putString(API_KEY, TEST_API_KEY);
        applicationInfo.metaData = metadataBundle;

        configureFritz(null);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(editor).putString(eq(SPKeys.FRITZ_SESSION), argument.capture());
        try {
            // Convert the captured string to a session
            Session actualSession = new Session(new JSONObject(argument.getValue()));

            // Check the API key was saved
            assertEquals(TEST_API_KEY, actualSession.getAppToken());
        } catch (JSONException e) {
            fail();
        }
    }

    /**
     * Test out fetching the api key from resources
     */
    @Test
    public void testConfigureWithApiKeyInResources() {
        Resources resources = mock(Resources.class);
        // Any number to simulate a resource id.
        int testResourceId = 6;
        when(resources.getIdentifier(API_KEY, "string", PACKAGE_NAME)).thenReturn(testResourceId);
        when(context.getResources()).thenReturn(resources);
        doReturn(TEST_API_KEY).when(context).getString(eq(testResourceId));

        // Call configure
        configureFritz(null);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(editor).putString(eq(SPKeys.FRITZ_SESSION), argument.capture());
        try {
            // Convert the captured string to a session
            Session actualSession = new Session(new JSONObject(argument.getValue()));

            // Check the API key was saved
            assertEquals(TEST_API_KEY, actualSession.getAppToken());
        } catch (JSONException e) {
            fail();
        }
    }

    /**
     * Test an exception is thrown when the api key resource is not found.
     */
    @Test(expected = RuntimeException.class)
    public void testConfigureResourceDNE() {
        Resources resources = mock(Resources.class);
        // Resource id = 0  which means it doesn't exist.
        int testResourceId = 0;
        when(resources.getIdentifier(API_KEY, "string", PACKAGE_NAME)).thenReturn(testResourceId);
        when(context.getResources()).thenReturn(resources);
        doReturn(TEST_API_KEY).when(context).getString(eq(testResourceId));

        // Calling configure will fail because the API key doesn't exist
        configureFritz(null);
    }

    /**
     * Check that an exception is thrown when Fritz is improperly configured
     */
    @Test(expected = RuntimeException.class)
    public void testErrorWhenNoApiKey() {
        configureFritz(null);
    }


}
