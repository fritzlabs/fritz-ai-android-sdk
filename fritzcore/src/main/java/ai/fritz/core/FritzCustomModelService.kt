package ai.fritz.core

import ai.fritz.core.FritzManagedModel.Companion.extractFromString
import ai.fritz.core.FritzOnDeviceModel.Companion.buildFromManagedModel
import ai.fritz.core.api.ApiClient
import ai.fritz.core.api.DownloadModelTask
import ai.fritz.core.api.RequestHandler
import ai.fritz.core.constants.ModelEventName
import ai.fritz.core.factories.ModelEventFactory
import ai.fritz.core.utils.FritzModelManager.Companion.getActiveVersionForModel
import ai.fritz.core.utils.FritzModelManager.Companion.updateFromServerConfigs
import ai.fritz.core.utils.JobUtil
import ai.fritz.core.utils.PreferenceManager.saveModel
import ai.fritz.core.utils.SessionPreferenceManager
import android.annotation.TargetApi
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import android.util.Log
import org.json.JSONException
import org.json.JSONObject

/**
 * Job Service for running OTA updates
 *
 * @hide
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class FritzCustomModelService : JobService() {
    override fun onStopJob(params: JobParameters): Boolean {
        Log.i(TAG, "Fritz Job Interrupted")
        // Broadcast that the job finished
        val intent = Intent()
        intent.action = FritzJobIntentActions.ON_JOB_FINISHED
        applicationContext.sendBroadcast(intent)
        return true
    }

    override fun onStartJob(params: JobParameters): Boolean {
        Log.d(TAG, "Starting Fritz Job")
        val bundle = params.extras
        val jobType = bundle.getString(JobUtil.JOB_TYPE_KEY)
        val managedModelBundle = bundle.getString(FritzManagedModel.MANAGED_MODEL_KEY)
        if (jobType == null || managedModelBundle == null) {
            return false
        }
        val managedModel = extractFromString(managedModelBundle)
        if (jobType == JobUtil.CHECK_MODEL_UPDATE_JOB) {
            checkModelUpdateJob(params, managedModel)
        }
        if (jobType == JobUtil.DOWNLOAD_MODEL_JOB) {
            downloadModel(params, managedModel)
        }
        // Job is running in the bg
        return true
    }

    private fun getSessionManager(): SessionManager {
        val session = SessionPreferenceManager.getSession(applicationContext)
                ?: throw RuntimeException("You must call Fritz.configure first.")
        var apiClient = ApiClient(session, applicationContext.getString(R.string.api_base) + "/sdk/v1")
        return SessionManager(applicationContext, session, apiClient)
    }

    private fun checkModelUpdateJob(params: JobParameters, managedModel: FritzManagedModel) {
        val client = getSessionManager().apiClient
        client.fetchActiveModelVersion(managedModel.modelId, managedModel.pinnedVersion, object : RequestHandler {
            override fun onSuccess(response: JSONObject?) {
                response ?: return
                try {
                    // Fetch the current active version
                    val currentVersion = getActiveVersionForModel(applicationContext, managedModel.modelId)
                    val configs = ModelDownloadConfigs(response)
                    updateFromServerConfigs(applicationContext, managedModel.modelId, configs.tags, configs.metadata)
                    managedModel.modelDownloadConfigs = configs
                    if (currentVersion != configs.modelVersion) {
                        downloadModel(params, managedModel)
                    } else {
                        Log.d(TAG, "Job Finished. No new versions detected.")
                        // Finish the job. No reschedule.
                        onJobFinishedNoUpdates(params)
                    }
                } catch (e: JSONException) {
                    Log.w(TAG, "Cannot process API response")
                    onJobFinishedNoUpdates(params)
                }
            }

            override fun onError(response: JSONObject?) {
                onJobFinishedFailure(params)
            }
        })
    }

    private fun downloadModel(params: JobParameters, fritzManagedModel: FritzManagedModel) {
        val sessionManager = getSessionManager()
        val downloadConfigs = fritzManagedModel.modelDownloadConfigs
        downloadConfigs ?: return
        Log.d(TAG, "Starting download for model version " + downloadConfigs.modelVersion)
        val downloadStartTime = System.nanoTime()
        val listener: DownloadModelTask.PostExecuteListener = object : DownloadModelTask.PostExecuteListener {
            override fun onSuccess(absoluteModelPath: String?) {
                val downloadTime = System.nanoTime() - downloadStartTime
                val onDeviceModel = buildFromManagedModel(
                        fritzManagedModel,
                        absoluteModelPath!!)
                val predictionTiming = ModelEventFactory.createCustomTimingEvent(ModelEventName.MODEL_DOWNLOAD_COMPLETED, onDeviceModel, downloadTime)
                sessionManager.track(predictionTiming)
                saveModel(applicationContext, onDeviceModel)
                onJobFinishedWithUpdates(params)
            }

            override fun onFailure() {
                Log.e(TAG, "Downloading model version " + downloadConfigs.modelVersion + " failed after " + (System.currentTimeMillis() - downloadStartTime) + "ms.")
                onJobFinishedFailure(params)
            }
        }
        val downloadTask = DownloadModelTask(fritzManagedModel.modelId, downloadConfigs.modelVersion, applicationContext.filesDir, listener)
        downloadTask.execute(downloadConfigs.urlToDownload)
    }

    private fun onJobFinishedFailure(params: JobParameters) {
        Log.e(TAG, "Job failed...rescheduling.")
        jobFinished(params, true)
    }

    private fun onJobFinishedNoUpdates(params: JobParameters) {
        val intent = Intent()
        intent.action = FritzJobIntentActions.ON_JOB_FINISHED
        applicationContext.sendBroadcast(intent)
        jobFinished(params, false)
    }

    private fun onJobFinishedWithUpdates(params: JobParameters) {
        val intent = Intent()
        intent.action = FritzJobIntentActions.ON_MODEL_DOWNLOAD
        applicationContext.sendBroadcast(intent)
        jobFinished(params, false)
    }

    companion object {
        val TAG = FritzCustomModelService::class.java.simpleName
    }
}