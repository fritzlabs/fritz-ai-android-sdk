package ai.fritz.core.api

import org.json.JSONObject

/**
 * Listener interface for success / error handling
 * @hide
 */
interface RequestHandler {
    fun onSuccess(response: JSONObject?)
    fun onError(response: JSONObject?)
}