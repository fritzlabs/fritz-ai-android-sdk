package ai.fritz.core.events

import android.annotation.TargetApi
import android.os.Build
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Generic event to track for a model
 *
 *
 * Format:
 * - type: name (prediction, model_installed)
 * - timestamp: time in ms
 * - data: EventData
 *
 * @hide
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class ModelEvent(val name: String, private val eventData: EventData, private val timestamp: Long) {
    @Throws(JSONException::class)
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("type", name)
        obj.put("timestamp", timestamp)
        obj.put("data", eventData.toJson())
        return obj
    }

    override fun toString(): String {
        return javaClass.simpleName + "(" + name + ")"
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (!ModelEvent::class.java.isAssignableFrom(other.javaClass)) {
            return false
        }
        val otherEvent = other as ModelEvent
        return name.equals(otherEvent.name, ignoreCase = true) && timestamp == otherEvent.timestamp
    }

    override fun hashCode(): Int {
        return Objects.hash(name, java.lang.Long.valueOf(timestamp), eventData)
    }

}