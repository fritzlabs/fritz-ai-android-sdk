package ai.fritz.core.events

import org.json.JSONException
import org.json.JSONObject

/**
 * All events should implement this interface
 * @hide
 */
interface EventData {
    @Throws(JSONException::class)
    fun toJson(): JSONObject
}