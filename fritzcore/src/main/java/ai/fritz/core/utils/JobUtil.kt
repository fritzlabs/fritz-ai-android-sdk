package ai.fritz.core.utils

import ai.fritz.core.Fritz.appContext
import ai.fritz.core.FritzCustomModelService
import ai.fritz.core.FritzManagedModel
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import android.util.Log
import java.util.concurrent.TimeUnit


const val TEN_SECONDS_MS: Long = 10 * 1000
const val THIRTY_MINUTES_IN_MS: Long = 30 * 60 * 1000

/**
 * Manages running jobs
 *
 * @hide
 */
object JobUtil {

    private val TAG = JobUtil::class.java.simpleName
    const val JOB_TYPE_KEY = "job_type"
    const val CHECK_MODEL_UPDATE_JOB = "check_model_update"
    const val DOWNLOAD_MODEL_JOB = "download_model"

    @JvmStatic
    @Synchronized
    fun checkForModelUpdate(appContext: Context?, fritzManagedModel: FritzManagedModel, useWifi: Boolean): Int {
        val jobScheduler = appContext!!.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val modelId = fritzManagedModel.modelId
        if (isJobScheduled(jobScheduler, modelId)) {
            Log.d(TAG, "Job is currently running")
            return JobScheduler.RESULT_FAILURE
        }
        val bundle = PersistableBundle()
        bundle.putString(JOB_TYPE_KEY, CHECK_MODEL_UPDATE_JOB)
        bundle.putString(FritzManagedModel.MANAGED_MODEL_KEY, fritzManagedModel.toJson().toString())
        val networkType = if (useWifi) JobInfo.NETWORK_TYPE_UNMETERED else JobInfo.NETWORK_TYPE_ANY
        // Note: https://stackoverflow.com/questions/33235754/jobscheduler-posting-jobs-twice-not-expected
        val jobInfo = JobInfo.Builder(getJobIdFromModelId(modelId), ComponentName(appContext, FritzCustomModelService::class.java))
                .setRequiredNetworkType(networkType)
                .setBackoffCriteria(TEN_SECONDS_MS, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
                .setOverrideDeadline(THIRTY_MINUTES_IN_MS)
                .setExtras(bundle)
                .build()
        return jobScheduler.schedule(jobInfo)
    }

    @Synchronized
    fun downloadModelVersion(fritzManagedModel: FritzManagedModel, useWifi: Boolean): Int {
        val jobScheduler = appContext!!.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        if (fritzManagedModel.modelDownloadConfigs == null) {
            Log.d(TAG, "Need model configs to download the model version")
            return JobScheduler.RESULT_FAILURE
        }
        if (isJobScheduled(jobScheduler, fritzManagedModel.modelId)) {
            Log.d(TAG, "Job is currently running")
            return JobScheduler.RESULT_FAILURE
        }
        val bundle = PersistableBundle()
        bundle.putString(JOB_TYPE_KEY, CHECK_MODEL_UPDATE_JOB)
        bundle.putString(FritzManagedModel.MANAGED_MODEL_KEY, fritzManagedModel.toJson().toString())
        val networkType = if (useWifi) JobInfo.NETWORK_TYPE_UNMETERED else JobInfo.NETWORK_TYPE_ANY
        // Note: https://stackoverflow.com/questions/33235754/jobscheduler-posting-jobs-twice-not-expected
        val jobInfo = JobInfo.Builder(getJobIdFromModelId(fritzManagedModel.modelId), ComponentName(
                appContext!!, FritzCustomModelService::class.java))
                .setRequiredNetworkType(networkType)
                .setBackoffCriteria(TEN_SECONDS_MS, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
                .setOverrideDeadline(THIRTY_MINUTES_IN_MS)
                .setExtras(bundle)
                .build()
        return jobScheduler.schedule(jobInfo)
    }

    private fun isJobScheduled(scheduler: JobScheduler, modelId: String): Boolean {
        val jobIdToFind = getJobIdFromModelId(modelId)
        for (jobInfo in scheduler.allPendingJobs) {
            val currentJobId = jobInfo.id
            if (currentJobId == jobIdToFind) {
                return true
            }
        }
        return false
    }

    private fun getJobIdFromModelId(modelId: String): Int {
        return modelId.hashCode()
    }

}