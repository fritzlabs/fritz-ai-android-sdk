package ai.fritz.core.utils

import ai.fritz.core.Fritz.appContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * @hide
 */
object JsonUtils {
    /**
     * Assuming the JSON array is a list of strings.
     */
    @JvmStatic
    @Throws(JSONException::class)
    fun convertJsonArrayToList(jsonArray: JSONArray): List<String> {
        val items: MutableList<String> = ArrayList()
        for (i in 0 until jsonArray.length()) {
            val name = jsonArray.getString(i)
            items.add(name)
        }
        return items
    }

    @JvmStatic
    @Throws(JSONException::class)
    fun convertListToJsonArray(list: List<String>): JSONArray {
        val array = JSONArray()
        for (item in list) {
            array.put(item)
        }
        return array
    }

    @JvmStatic
    fun buildFromJsonFile(assetPath: String): JSONObject {
        val context = appContext
        return try {
            val inputStream = context!!.assets.open(assetPath)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, Charsets.UTF_8)
            JSONObject(json)
        } catch (e: IOException) {
            throw RuntimeException("Unable to load model config file from: $assetPath")
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    @Throws(JSONException::class)
    fun toMap(jsonObject: JSONObject): Map<String, String> {
        val map: MutableMap<String, String> = HashMap()
        val keysItr = jsonObject.keys()
        while (keysItr.hasNext()) {
            val key = keysItr.next()
            val value = jsonObject.getString(key)
            map[key] = value
        }
        return map
    }

    @JvmStatic
    fun getJSONObject(jsonObject: JSONObject, key: String): JSONObject? {
        return try {
            if (jsonObject.has(key)) {
                jsonObject.getJSONObject(key)
            } else null
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }
}