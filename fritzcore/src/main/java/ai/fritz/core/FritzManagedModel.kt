package ai.fritz.core

import org.json.JSONException
import org.json.JSONObject

const val MODEL_ID_KEY = "model_id"
const val PINNED_VERSION_KEY = "model_pinned_version"
const val MODEL_DOWNLOAD_CONFIGS = "model_download_configs"

/**
 * FritzManagedModel objects link to a custom model created in Fritz.
 *
 *
 * By passing a FritzManagedModel into [FritzModelManager]
 * and calling [FritzModelManager.loadModel]
 * to download a model at runtime.
 */
open class FritzManagedModel(var modelId: String,
                             var pinnedVersion: Int? = null, var modelDownloadConfigs: ModelDownloadConfigs? = null) {

    constructor(modelId: String) : this(modelId, null, null);
    constructor(modelId: String, pinnedVersion: Int?) : this(modelId, pinnedVersion, null);

    open fun toJson(): JSONObject? {
        return try {
            val jsonObject = JSONObject()
            jsonObject.put(MODEL_ID_KEY, modelId)
            jsonObject.put(PINNED_VERSION_KEY, pinnedVersion)
            val modelDownloadConfigsPayload = modelDownloadConfigs?.toJson()
            jsonObject.put(MODEL_DOWNLOAD_CONFIGS, modelDownloadConfigsPayload)
            jsonObject
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    companion object {

        const val MANAGED_MODEL_KEY = "managed_model"

        fun extractFromJson(jsonObject: JSONObject): FritzManagedModel {
            return try {
                val modelId = jsonObject.getString(MODEL_ID_KEY)
                var pinnedVersion: Int? = null
                if (jsonObject.has(PINNED_VERSION_KEY)) {
                    pinnedVersion = jsonObject.getInt(PINNED_VERSION_KEY)
                }
                var downloadConfigs: ModelDownloadConfigs? = null
                if (jsonObject.has(MODEL_DOWNLOAD_CONFIGS)) {
                    val modelDownloadConfigsPayload = jsonObject.getJSONObject(MODEL_DOWNLOAD_CONFIGS)
                    downloadConfigs = ModelDownloadConfigs(modelDownloadConfigsPayload)
                }
                FritzManagedModel(modelId, pinnedVersion, downloadConfigs)
            } catch (e: JSONException) {
                throw RuntimeException(e)
            }
        }

        @JvmStatic
        fun extractFromString(value: String): FritzManagedModel {
            return try {
                val jsonObject = JSONObject(value)
                extractFromJson(jsonObject)
            } catch (e: JSONException) {
                throw RuntimeException(e)
            }
        }
    }
}