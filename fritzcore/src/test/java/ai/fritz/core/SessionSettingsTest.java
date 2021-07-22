package ai.fritz.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ai.fritz.core.api.SessionSettings;
import ai.fritz.core.constants.ModelEventName;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 27, packageName = "ai.fritz.sdkapp")
public class SessionSettingsTest {
    private static final long DEFAULT_SETTINGS_REFRESH_INTERVAL = TimeUnit.MINUTES.toMillis(30);


    private static final String SETTINGS_REFRESH_INTERVAL_KEY = "settings_refresh_interval";
    private static final String API_REQUESTS_ENABLED_KEY = "api_requests_enabled";
    private static final String IO_SAMPLING_KEY = "model_input_output_sampling_ratio";
    private static final String TRACK_REQUEST_BATCH_SIZE_KEY = "track_request_batch_size";
    private static final String GZIP_KEY = "gzip_track_events";
    private static final String BATCH_FLUSH_INTERVAL_KEY = "batch_flush_interval";
    private static final String SETTINGS_LAST_CHECKED_AT_KEY = "settings_last_checked_at";
    private static final String EVENT_BLACKLIST = "event_blacklist";

    @Test
    public void testFromResponse() throws JSONException {
        JSONObject expectedValues = getTestResponse();
        int settingsRefreshInMinutes = expectedValues.getInt(SETTINGS_REFRESH_INTERVAL_KEY);
        SessionSettings sessionSettings = SessionSettings.fromResponse(expectedValues);
        // Check that we converted it into ms
        assertEquals(sessionSettings.getSettingsRefreshInterval(), TimeUnit.MINUTES.toMillis(settingsRefreshInMinutes));
        // Check that we got the last checked point.
        assertEquals(sessionSettings.getSettingsLastCheckedAt(), expectedValues.getLong(SETTINGS_LAST_CHECKED_AT_KEY));

        List<String> eventBlacklist = sessionSettings.getEventBlacklist();
        List<String> expectedBlacklistEvents = new ArrayList<>();
        expectedBlacklistEvents.add(ModelEventName.MODEL_DOWNLOAD_COMPLETED.name());
        expectedBlacklistEvents.add(ModelEventName.MODEL_PREPROCESS.name());
        expectedBlacklistEvents.add(ModelEventName.MODEL_POSTPROCESS.name());

        assertEquals(eventBlacklist.size(), expectedBlacklistEvents.size());
    }

    @Test
    public void testFromSharedPreferences() throws JSONException {
        JSONObject expectedValues = getTestResponse();
        int settingsRefreshInMinutes = expectedValues.getInt(SETTINGS_REFRESH_INTERVAL_KEY);
        SessionSettings sessionSettings = SessionSettings.fromSharedPreferences(expectedValues);

        // No change if we're building it from shared preferences.
        assertEquals(sessionSettings.getSettingsRefreshInterval(), settingsRefreshInMinutes);
        // Check that we got the last checked point.
        assertEquals(sessionSettings.getSettingsLastCheckedAt(), expectedValues.getLong(SETTINGS_LAST_CHECKED_AT_KEY));
    }

    @Test
    public void testCreateDefault() {
        SessionSettings sessionSettings = SessionSettings.createDefault();
        assertEquals(sessionSettings.getSettingsLastCheckedAt(), 0);
        assertEquals(sessionSettings.getSettingsRefreshInterval(), DEFAULT_SETTINGS_REFRESH_INTERVAL);
    }

    private JSONObject getTestResponse() throws JSONException {
        JSONObject object = new JSONObject();

        JSONArray eventBlacklist = new JSONArray();
        eventBlacklist.put(ModelEventName.MODEL_DOWNLOAD_COMPLETED);
        eventBlacklist.put(ModelEventName.MODEL_PREPROCESS);
        eventBlacklist.put(ModelEventName.MODEL_POSTPROCESS);

        // In minutes
        object.put(SETTINGS_REFRESH_INTERVAL_KEY, 30);
        object.put(API_REQUESTS_ENABLED_KEY, true);
        object.put(IO_SAMPLING_KEY, 0.1);
        object.put(TRACK_REQUEST_BATCH_SIZE_KEY, 1000);
        object.put(GZIP_KEY, true);
        object.put(BATCH_FLUSH_INTERVAL_KEY, 100);
        object.put(SETTINGS_LAST_CHECKED_AT_KEY, System.currentTimeMillis());
        object.put(EVENT_BLACKLIST, eventBlacklist);

        return object;
    }
}
