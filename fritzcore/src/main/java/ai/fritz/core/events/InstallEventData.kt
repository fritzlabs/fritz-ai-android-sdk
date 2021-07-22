package ai.fritz.core.events

import org.json.JSONException
import org.json.JSONObject

/**
 * Track model install events
 * @hide
 */
class InstallEventData(private val modelUid: String, private val modelVersion: Int, private val isOta: Boolean) : EventData {
    @Throws(JSONException::class)
    override fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("model_uid", modelUid)
        obj.put("model_version", modelVersion)
        obj.put("is_ota", isOta)
        return obj
    }

}