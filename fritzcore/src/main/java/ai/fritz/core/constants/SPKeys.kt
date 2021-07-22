package ai.fritz.core.constants

import ai.fritz.core.FritzOnDeviceModel

/**
 * Shared Preference Keys to manage the SDK state
 *
 * @hide
 */
object SPKeys {
    const val BUNDLE_ID = "com.fritz.core"
    const val FRITZ_SESSION = "fritz_session"

    @JvmStatic
    fun getModelKey(fritzOnDeviceModel: FritzOnDeviceModel): String {
        return "custom_model_key_" + fritzOnDeviceModel.modelId
    }

    @JvmStatic
    fun getModelKeyById(modelId: String): String {
        return "custom_model_key_$modelId"
    }

    @JvmStatic
    fun getActiveVersionForModelKey(fritzOnDeviceModel: FritzOnDeviceModel): String {
        return "custom_model_active_version_for_" + fritzOnDeviceModel.modelId
    }

    @JvmStatic
    fun getActiveVersionForModelKey(modelId: String): String {
        return "custom_model_active_version_for_$modelId"
    }

    @JvmStatic
    fun getHasTrackedModelVersionKey(modelId: String, modelVersion: Int): String {
        return "has_tracked_model_" + modelId + "_version_" + modelVersion
    }
}