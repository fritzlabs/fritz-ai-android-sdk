package ai.fritz.core.events

import org.json.JSONException
import org.json.JSONObject

/**
 * Tracks the timing event data
 *
 * @hide
 */
class ModelTimingEventData(var modelUid: String, var modelVersion: Int, var elapsedNs: Long) : EventData {

    @Throws(JSONException::class)
    override fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("model_uid", modelUid)
        obj.put("model_version", modelVersion)
        obj.put("elapsed_nano_seconds", elapsedNs)
        return obj
    }

}