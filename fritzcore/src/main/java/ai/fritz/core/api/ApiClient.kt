package ai.fritz.core.api

import ai.fritz.core.Fritz
import ai.fritz.core.api.ErrorMessages.sessionSettingsFailureMessage
import ai.fritz.core.api.SessionSettings.Companion.fromResponse
import ai.fritz.core.events.ModelEvent
import ai.fritz.core.utils.SessionPreferenceManager.updateSessionSettings
import android.text.TextUtils
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.util.*
import javax.net.ssl.HttpsURLConnection

/**
 * Client for the Fritz service.
 *
 * @hide
 */
open class ApiClient(val session: Session, private val apiBase: String) {
    /**
     * Track events
     *
     * @param events  - a list of model events
     * @param handler - handler for the request
     */
    open fun batchTracking(events: List<ModelEvent>, handler: RequestHandler) {
        if (!session.isApiEnabled) {
            Log.d(TAG, "Fritz is not currently recording events")
            handler.onError(null)
            return
        }
    }

    fun recordAnnotationEvent(modelEvent: ModelEvent, handler: RequestHandler) {
        val data = JSONArray()
        data.put(modelEvent.toJson())

        try {
            val url = URL("$apiBase/model/annotation")
            val payload = JSONObject()
            payload.put("data", data)
            PostRequestTask(session, handler).execute(Request(url, payload))
        } catch (e: JSONException) {
            throw RuntimeException(e)
        } catch (e: MalformedURLException) {
            throw RuntimeException(e)
        }
    }

    fun fetchActiveModelVersion(modelId: String, pinnedVersion: Int?, handler: RequestHandler) {
        if (!session.isApiEnabled) {
            Log.d(TAG, "Fritz is not currently recording events")
            handler.onError(null)
            return
        }
    }

    fun queryModelsByTags(tags: Array<String>, handler: RequestHandler) {
        val sanitizedTags: MutableList<String?> = ArrayList()
        for (tag in tags) {
            try {
                sanitizedTags.add(URLEncoder.encode(tag, "UTF-8"))
            } catch (e: UnsupportedEncodingException) {
                Log.e(TAG, e.toString())
            }
        }
        val tagsSeparated = TextUtils.join(",", sanitizedTags)
        if (!session.isApiEnabled) {
            Log.d(TAG, "Fritz is not currently recording events")
            handler.onError(null)
            return
        }
    }

    open fun fetchSettings() {
        // Don't check settings if it isn't time yet.
        if (!session.settings.shouldCheckSettings()) {
            return
        }
        Log.d(TAG, "Fritz AI backend disabled.")
    }

    companion object {
        private val TAG = ApiClient::class.java.simpleName
    }
}