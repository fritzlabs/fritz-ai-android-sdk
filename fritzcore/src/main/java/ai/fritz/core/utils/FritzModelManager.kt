package ai.fritz.core.utils

import ai.fritz.core.*
import ai.fritz.core.Fritz.appContext
import ai.fritz.core.constants.SPKeys
import ai.fritz.core.factories.ModelEventFactory
import android.content.Context
import android.os.Handler
import android.os.Looper
import java.io.File

class FritzModelManager(managedModel: FritzManagedModel) {
    var modelId: String
    private val pinnedVersion: Int?
    private val modelDownloadConfigs: ModelDownloadConfigs?
    var currentOnDeviceModel: FritzOnDeviceModel?
    var modelDownloadManager: ModelDownloadManager
    /**
     * Download the latest model version.
     *
     * @param statusListener - a callback when the download finishes or when an onDevice model exists.
     * @param useWifi        - if the download should only happen with wifi.
     */
    /**
     * Download the latest model version.
     *
     * @param statusListener
     */
    @JvmOverloads
    fun loadModel(statusListener: ModelReadyListener, useWifi: Boolean = false) {
        if (currentOnDeviceModel != null && currentOnDeviceModel!!.pinnedVersion == pinnedVersion) {
            statusListener.onModelReady(currentOnDeviceModel!!)
            return
        }
    }

    companion object {
        private val TAG = FritzModelManager::class.java.simpleName
        /**
         * Get the active model version saved on device.
         *
         *
         * If none exists, 0 is returned.
         *
         * @param context
         * @param modelId
         * @return the version number.
         */
        @JvmStatic
        fun getActiveVersionForModel(context: Context, modelId: String): Int {
            return PreferenceManager.getInt(context, SPKeys.getActiveVersionForModelKey(modelId))
        }

        /**
         * Get the active [FritzOnDeviceModel] saved on device.
         *
         * @param modelId
         * @return an optional on device model.
         */
        @JvmStatic
        fun getActiveOnDeviceModel(modelId: String): FritzOnDeviceModel? {
            return PreferenceManager.getSavedModel(appContext!!, modelId)
        }

        /**
         * Update the on-device model with metadata + tags.
         *
         * @param context
         * @param modelId
         * @param tags
         * @param updatedMetadata
         * @hide
         */
        @JvmStatic
        fun updateFromServerConfigs(context: Context, modelId: String, tags: List<String>?, updatedMetadata: Map<String, String>?) {
            val model: FritzOnDeviceModel? = PreferenceManager.getSavedModel(context, modelId) ?: return
            // Update the metadata field with the newest info
            model?.metadata = updatedMetadata
            model?.tags = tags
            PreferenceManager.saveModel(context, model)
        }

        /**
         * When an on device model is initialized, handle install if it's a first time initialization and update the information
         * according to the newer model.
         *
         * @param includedOnDeviceModel
         * @hide
         */
        @JvmStatic
        fun handleModelInitialized(includedOnDeviceModel: FritzOnDeviceModel) {
            val appContext = appContext!!
            val activeModel = PreferenceManager.getSavedModel(appContext, includedOnDeviceModel.modelId)
            if (!PreferenceManager.hasTrackedModelVersionInstall(appContext, includedOnDeviceModel)) {
                val installEvent = ModelEventFactory.createInstallEvent(includedOnDeviceModel)
                Fritz.sessionManager.track(installEvent)
                PreferenceManager.saveHasTrackedModelVersionInstall(appContext, includedOnDeviceModel, true)
            }
            // If there's no active custom model stored, set the newly created one as active
            if (activeModel == null) {
                PreferenceManager.saveModel(appContext, includedOnDeviceModel)
                return
            }
            // Get existing on device model
            // Ensure the saved model file always exists, if it doesn't, use the included, on-device model
            // This can happen when someone swaps out the model during development. If a user updates the app
            // and the old model is no longer in the assets folder, this protects the app from loading a bad file.
            val file = File(activeModel.modelPath)
            // If the file does not exist or the model versions are equal, update the model to the new one passed in
            // (updates model path if it's changed)
            if (!file.exists() || activeModel.modelVersion == includedOnDeviceModel.modelVersion) {
                PreferenceManager.saveModel(appContext, includedOnDeviceModel)
            }
        }
    }

    init {
        currentOnDeviceModel = getActiveOnDeviceModel(managedModel.modelId)
        modelId = managedModel.modelId
        pinnedVersion = managedModel.pinnedVersion
        modelDownloadConfigs = managedModel.modelDownloadConfigs
        modelDownloadManager = ModelDownloadManager(managedModel)
    }
}