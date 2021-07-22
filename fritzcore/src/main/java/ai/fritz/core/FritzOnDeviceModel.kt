package ai.fritz.core

import ai.fritz.core.utils.JsonUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

const val MODEL_PATH_KEY = "model_path"
const val MODEL_VERSION_KEY = "model_version"
const val MODEL_METADATA_KEY = "model_metadata"
const val MODEL_TAGS_KEY = "model_tags"
const val IS_DOWNLOADED_OTA_KEY = "model_is_download_ota"
const val ANDROID_ASSET_ROOT = "file:///android_asset/"

/**
 * FritzOnDeviceModels represent models that have been loaded onto the device.
 *
 *
 * They store the model id, the version, tags + metadata, and the path to the specific model (.pb, .tflite).
 */
open class FritzOnDeviceModel(var modelPath: String, modelId: String, var modelVersion: Int) : FritzManagedModel(modelId) {
    var metadata: Map<String, String>? = HashMap()
    var tags: List<String>? = ArrayList()
    var isDownloadedOTA = false
        private set
    /**
     * If the model path provided is for an encrypted model, specify the encryption seed to use the model.
     *
     * @param seed
     */
    var encryptionSeed: ByteArray? = null;

    /**
     * Creates an on-device model.
     *
     * @param modelPath     the file path of this model
     * @param modelId       the ID of this model
     * @param modelVersion  the version of this model
     * @param pinnedVersion the target version for this model
     */
    constructor(modelPath: String, modelId: String, modelVersion: Int, pinnedVersion: Int? = null) : this(modelPath, modelId, modelVersion) {
        this.pinnedVersion = pinnedVersion
    }

    /**
     * Creates an on-device model.
     * Specifying a pinned version will override usage of the packaged version,
     * granting the SDK flexibility in regards to downloading specific model versions.
     *
     * @param modelPath     the file path of this model
     * @param modelId       the ID of this model
     * @param modelVersion  the version of this model
     * @param pinnedVersion the target version for this model
     * @param metadata      existing metadata for the model
     * @param tags          any tags for the model
     * @param isOTA         whether or not the model can be downloaded over-the-air
     */
    constructor(modelPath: String, modelId: String, modelVersion: Int, pinnedVersion: Int? = null, metadata: Map<String, String>? = null, tags: List<String>? = null, isOTA: Boolean = false) : this(modelPath, modelId, modelVersion) {
        this.pinnedVersion = pinnedVersion
        this.metadata = metadata
        this.tags = tags
        this.isDownloadedOTA = isOTA
    }


    override fun toString(): String {
        return "Model $modelId(version: $modelVersion)"
    }

    /**
     * Convert the FritzOnDeviceModel object to a JSON Object for storage.
     *
     * @return the FritzOnDeviceModel as a JSON object
     * @hide
     */
    override fun toJson(): JSONObject? {
        val json = super.toJson()
        try {
            json!!.put(MODEL_PATH_KEY, modelPath)
            json.put(MODEL_VERSION_KEY, modelVersion)
            json.put(MODEL_METADATA_KEY, JSONObject(metadata))
            json.put(MODEL_TAGS_KEY, JSONArray(tags))
            json.put(IS_DOWNLOADED_OTA_KEY, isDownloadedOTA)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
        return json
    }

    val isEncrypted: Boolean
        get() = encryptionSeed != null

    /**
     * Checks if the custom model are equal.
     *
     * @param other the other FritzOnDeviceModel
     * @return true if equal, false otherwise
     * @hide
     */
    override fun equals(other: Any?): Boolean {
        if (other is FritzOnDeviceModel) {
            val otherSettings = other
            return otherSettings.modelId.equals(modelId, ignoreCase = true) && otherSettings.modelVersion == modelVersion && otherSettings.modelPath == modelPath
        }
        return false
    }

    /**
     * Delete the model file in storage.
     *
     * @return true/false if deleted
     * @hide
     */
    fun deleteModelFile(): Boolean { // Don't delete a file in the assets folder (bundled with the app)
        if (modelPath == null || modelPath!!.startsWith(ANDROID_ASSET_ROOT)) {
            return false
        }
        val modelFile = File(modelPath)
        return modelFile.delete()
    }

    companion object {

        /**
         * Create a FritzOnDeviceModel object through a JSONObject.
         *
         *
         * Useful if you'd like to rebuild FritzOnDeviceModel from storage.
         *
         * @param jsonObject the JSON object to rebuild from
         * @return the rebuilt JSON object
         * @hide
         */
        @JvmStatic
        fun buildFromJson(jsonObject: JSONObject): FritzOnDeviceModel {
            return try {
                val modelPath = jsonObject.getString(MODEL_PATH_KEY)
                val modelId = jsonObject.getString(MODEL_ID_KEY)
                val modelVersion = jsonObject.getInt(MODEL_VERSION_KEY)

                val pinnedVersion = if (jsonObject.has(PINNED_VERSION_KEY)) jsonObject.getInt(PINNED_VERSION_KEY) else null
                val metadata = if (jsonObject.has(MODEL_METADATA_KEY)) JsonUtils.toMap(jsonObject.getJSONObject(MODEL_METADATA_KEY)) else null
                val modelTagsJson = if (jsonObject.has(MODEL_TAGS_KEY)) jsonObject.getJSONArray(MODEL_TAGS_KEY) else null
                val tagNames = if (modelTagsJson != null) JsonUtils.convertJsonArrayToList(modelTagsJson) else ArrayList()
                val isOTA = jsonObject.has(IS_DOWNLOADED_OTA_KEY) && jsonObject.getBoolean(IS_DOWNLOADED_OTA_KEY)
                FritzOnDeviceModel(modelPath, modelId, modelVersion, pinnedVersion, metadata, tagNames, isOTA)
            } catch (e: JSONException) {
                throw RuntimeException(e)
            }
        }

        @JvmStatic
        fun buildFromModelConfig(assetPath: String): FritzOnDeviceModel {
            val modelConfig = JsonUtils.buildFromJsonFile(assetPath)
            return buildFromModelConfig(modelConfig)
        }

        @JvmStatic
        fun buildFromModelConfig(jsonObject: JSONObject): FritzOnDeviceModel {
            return try {
                val modelId = jsonObject.getString("model_id")
                val modelPath = jsonObject.getString("model_path")
                val modelVersion = jsonObject.getInt("model_version")
                val pinnedVersion = jsonObject.getInt("pinned_version")
                FritzOnDeviceModel(modelPath, modelId, modelVersion, pinnedVersion)
            } catch (e: JSONException) {
                throw RuntimeException(e)
            }
        }

        @JvmStatic
        fun buildFromManagedModel(managedModel: FritzManagedModel, modelPath: String): FritzOnDeviceModel {
            return FritzOnDeviceModel(
                    modelPath,
                    managedModel.modelId,
                    managedModel.modelDownloadConfigs!!.modelVersion,
                    managedModel.pinnedVersion,
                    managedModel.modelDownloadConfigs!!.metadata,
                    managedModel.modelDownloadConfigs!!.tags,
                    true)
        }
    }
}