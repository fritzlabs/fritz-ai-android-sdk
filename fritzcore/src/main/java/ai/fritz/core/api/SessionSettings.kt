package ai.fritz.core.api

import ai.fritz.core.utils.JsonUtils.convertJsonArrayToList
import ai.fritz.core.utils.JsonUtils.convertListToJsonArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * These are configurable options set from the settings endpoint.
 *
 * @hide
 */
class SessionSettings {
    var settingsRefreshInterval: Long = 0
        private set
    var isEnabledApiRequests = false
        private set
    var modelInputOutputSamplingRatio = 0.0
        private set
    var trackRequestBatchSize = 0
        private set
    var isGzipTrackEvents = false
        private set
    var batchFlushInterval: Long = 0
        private set
    var eventBlacklist: List<String>? = null
    // keeps track of the last time we refreshed the settings
    var settingsLastCheckedAt = 0L

    private constructor() {
        settingsRefreshInterval = DEFAULT_SETTINGS_REFRESH_INTERVAL
        isEnabledApiRequests = DEFAULT_API_REQUESTS_ENABLED
        modelInputOutputSamplingRatio = DEFAULT_INPUT_OUTPUT_SAMPLING_RATIO
        trackRequestBatchSize = DEFAULT_TRACK_REQUEST_BATCH_SIZE
        isGzipTrackEvents = DEFAULT_GZIP_TRACK_EVENTS
        batchFlushInterval = DEFAULT_BATCH_FLUSH_INTERVAL
        eventBlacklist = DEFAULT_EVENT_BLACKLIST
    }

    private constructor(settings: JSONObject) {
        if (settings.has(API_REQUESTS_ENABLED_KEY)) {
            isEnabledApiRequests = settings.getBoolean(API_REQUESTS_ENABLED_KEY)
        }
        if (settings.has(SETTINGS_REFRESH_INTERVAL_KEY)) {
            settingsRefreshInterval = settings.getLong(SETTINGS_REFRESH_INTERVAL_KEY)
        }
        if (settings.has(IO_SAMPLING_KEY)) {
            modelInputOutputSamplingRatio = settings.getDouble(IO_SAMPLING_KEY)
        }
        if (settings.has(TRACK_REQUEST_BATCH_SIZE_KEY)) {
            trackRequestBatchSize = settings.getInt(TRACK_REQUEST_BATCH_SIZE_KEY)
        }
        if (settings.has(GZIP_KEY)) {
            isGzipTrackEvents = settings.getBoolean(GZIP_KEY)
        }
        if (settings.has(BATCH_FLUSH_INTERVAL_KEY)) {
            batchFlushInterval = settings.getLong(BATCH_FLUSH_INTERVAL_KEY)
        }
        if (settings.has(SETTINGS_LAST_CHECKED_AT_KEY)) {
            settingsLastCheckedAt = settings.getLong(SETTINGS_LAST_CHECKED_AT_KEY)
        }
        if (settings.has(EVENT_BLACKLIST_KEY)) {
            eventBlacklist = convertJsonArrayToList(settings.getJSONArray(EVENT_BLACKLIST_KEY))
        }
    }

    fun shouldCheckSettings(): Boolean {
        val now = System.currentTimeMillis()
        val difference = now - settingsLastCheckedAt
        return difference >= settingsRefreshInterval
    }

    @Throws(JSONException::class)
    fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put(API_REQUESTS_ENABLED_KEY, isEnabledApiRequests)
        jsonObject.put(SETTINGS_REFRESH_INTERVAL_KEY, settingsRefreshInterval)
        jsonObject.put(IO_SAMPLING_KEY, modelInputOutputSamplingRatio)
        jsonObject.put(TRACK_REQUEST_BATCH_SIZE_KEY, trackRequestBatchSize)
        jsonObject.put(GZIP_KEY, isGzipTrackEvents)
        jsonObject.put(BATCH_FLUSH_INTERVAL_KEY, batchFlushInterval)
        jsonObject.put(SETTINGS_LAST_CHECKED_AT_KEY, settingsLastCheckedAt)
        jsonObject.put(EVENT_BLACKLIST_KEY, convertListToJsonArray(eventBlacklist!!))
        return jsonObject
    }

    /**
     * Checks if the sessions are equal.
     *
     * @param other The other session
     * @return true if equal, false otherwise
     * @hide
     */
    override fun equals(other: Any?): Boolean {
        if (other is SessionSettings) {
            val otherSessionSettings = other
            if (otherSessionSettings.batchFlushInterval == batchFlushInterval && otherSessionSettings.modelInputOutputSamplingRatio == modelInputOutputSamplingRatio && otherSessionSettings.settingsLastCheckedAt == settingsLastCheckedAt && otherSessionSettings.settingsRefreshInterval == settingsRefreshInterval && otherSessionSettings.isEnabledApiRequests == isEnabledApiRequests && otherSessionSettings.isGzipTrackEvents == isGzipTrackEvents && otherSessionSettings.trackRequestBatchSize == trackRequestBatchSize &&
                    otherSessionSettings.eventBlacklist!!.containsAll(eventBlacklist!!) && otherSessionSettings.eventBlacklist!!.size == eventBlacklist!!.size) {
                return true
            }
        }
        return false
    }

    companion object {
        private val TAG = SessionSettings::class.java.simpleName
        // The keys to match up to the response payload
        private const val SETTINGS_REFRESH_INTERVAL_KEY = "settings_refresh_interval"
        private const val API_REQUESTS_ENABLED_KEY = "api_requests_enabled"
        private const val IO_SAMPLING_KEY = "model_input_output_sampling_ratio"
        private const val TRACK_REQUEST_BATCH_SIZE_KEY = "track_request_batch_size"
        private const val GZIP_KEY = "gzip_track_events"
        private const val BATCH_FLUSH_INTERVAL_KEY = "batch_flush_interval"
        private const val SETTINGS_LAST_CHECKED_AT_KEY = "settings_last_checked_at"
        private const val EVENT_BLACKLIST_KEY = "event_blacklist"
        // Default settings
        val DEFAULT_SETTINGS_REFRESH_INTERVAL = TimeUnit.MINUTES.toMillis(30)
        const val DEFAULT_API_REQUESTS_ENABLED = false
        const val DEFAULT_INPUT_OUTPUT_SAMPLING_RATIO = 0.0
        const val DEFAULT_TRACK_REQUEST_BATCH_SIZE = 100
        const val DEFAULT_GZIP_TRACK_EVENTS = false
        val DEFAULT_BATCH_FLUSH_INTERVAL = TimeUnit.MINUTES.toMillis(1)
        val DEFAULT_EVENT_BLACKLIST: List<String> = ArrayList()
        @JvmStatic
        @Throws(JSONException::class)
        fun fromResponse(settings: JSONObject): SessionSettings { // Convert the refresh interval to millis from minutes
            if (settings.has(SETTINGS_REFRESH_INTERVAL_KEY)) {
                val refreshIntervalMin = settings.getLong(SETTINGS_REFRESH_INTERVAL_KEY)
                // Convert minutes to millis
                val refreshIntervalMillis = TimeUnit.MINUTES.toMillis(refreshIntervalMin)
                settings.put(SETTINGS_REFRESH_INTERVAL_KEY, refreshIntervalMillis)
            }
            // Convert batch flush interval to millis from seconds
            if (settings.has(BATCH_FLUSH_INTERVAL_KEY)) {
                val batchFlushInterval = settings.getLong(BATCH_FLUSH_INTERVAL_KEY)
                // Convert seconds to millis
                val batchFlushIntervalMillis = TimeUnit.SECONDS.toMillis(batchFlushInterval)
                settings.put(BATCH_FLUSH_INTERVAL_KEY, batchFlushIntervalMillis)
            }
            return SessionSettings(settings)
        }

        @JvmStatic
        fun createDefault(): SessionSettings {
            return SessionSettings()
        }

        @JvmStatic
        @Throws(JSONException::class)
        fun fromSharedPreferences(storedSettings: JSONObject): SessionSettings {
            return SessionSettings(storedSettings)
        }
    }
}