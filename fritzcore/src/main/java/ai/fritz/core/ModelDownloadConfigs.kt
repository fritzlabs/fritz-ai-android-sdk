package ai.fritz.core

import ai.fritz.core.utils.JsonUtils.convertJsonArrayToList
import ai.fritz.core.utils.JsonUtils.toMap
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ModelDownloadConfigs {
    var modelVersion: Int
    var urlToDownload: String
    var metadata: Map<String, String>
    var tags: List<String>

    constructor(modelVersion: Int, urlToDownload: String, metadata: Map<String, String>, tags: List<String>) {
        this.modelVersion = modelVersion
        this.urlToDownload = urlToDownload
        this.metadata = metadata
        this.tags = tags
    }

    constructor(modelVersionObject: JSONObject?) {
        modelVersion = modelVersionObject!!.getInt("version")
        urlToDownload = modelVersionObject.getString("src")
        val metadata = modelVersionObject.getJSONObject("metadata")
        this.metadata = if (metadata != null) toMap(modelVersionObject.getJSONObject("metadata")) else HashMap<String, String>()
        val tagsJson = modelVersionObject.getJSONArray("tags")
        tags = tagsJson?.let { convertJsonArrayToList(it) } ?: ArrayList()
    }

    fun toJson(): JSONObject {
        return try {
            val `object` = JSONObject()
            `object`.put(MODEL_VERSION, modelVersion)
            `object`.put(MODEL_URL_TO_DOWNLOAD, urlToDownload)
            `object`.put(MODEL_METADATA, JSONObject(metadata).toString())
            `object`.put(MODEL_TAGS, tags.toTypedArray())
            `object`
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        const val MODEL_VERSION = "model_version"
        const val MODEL_URL_TO_DOWNLOAD = "model_url_to_download"
        const val MODEL_METADATA = "model_metadata"
        const val MODEL_TAGS = "model_tags"
    }
}