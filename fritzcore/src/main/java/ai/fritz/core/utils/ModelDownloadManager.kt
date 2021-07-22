package ai.fritz.core.utils

import ai.fritz.core.Fritz.appContext
import ai.fritz.core.Fritz.enableOTAModelUpdates
import ai.fritz.core.FritzJobIntentActions
import ai.fritz.core.FritzManagedModel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Handles downloading models with the [ai.fritz.core.FritzCustomModelService].
 * This class is also a broadcast receiver to fetch updates when model downloads complete.
 *
 * @hide
 */
class ModelDownloadManager(private val managedModel: FritzManagedModel) : BroadcastReceiver() {
    // Note: This is a local variable so if Fritz.getInterpreter is used in the scope
// of the activity lifecycle (e.g. onCreate) we will run the api call again.
    private var lastUpdateCheckedAt = 0L
    private var useWifi = false
    private val isReceiverRegistered = AtomicBoolean(false)
    private var statusChangeListener: OnModelStatusChange? = null

    interface OnModelStatusChange {
        fun onDownloaded()
    }

    fun setStatusChangeListener(listener: OnModelStatusChange?) {
        statusChangeListener = listener
    }

    fun setUseWifi(useWifi: Boolean) {
        this.useWifi = useWifi
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, intent.action)
        if (FritzJobIntentActions.ON_MODEL_DOWNLOAD.equals(intent.action, ignoreCase = true)) {
            if (statusChangeListener != null) {
                statusChangeListener!!.onDownloaded()
            }
        }
        // Unregister this receiver.
        if (isReceiverRegistered.compareAndSet(true, false)) {
            appContext?.unregisterReceiver(this)
        }
    }

    private fun registerJobServiceReceiver() {
        val filter = IntentFilter()
        filter.addAction(FritzJobIntentActions.ON_MODEL_DOWNLOAD)
        filter.addAction(FritzJobIntentActions.ON_JOB_FINISHED)
        if (isReceiverRegistered.compareAndSet(false, true)) {
            appContext?.registerReceiver(this, filter)
        }
    }

    fun checkForNewActiveVersion(): Boolean { // Ignore model updates.
        if (!enableOTAModelUpdates) {
            return false
        }
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - lastUpdateCheckedAt
        // Check for a new model version download
        if (timeDiff >= WAIT_BETWEEN_MODEL_VERSION_CHECKS) {
            launchCheckUpdateJob()
            return true
        }
        return false
    }

    fun launchCheckUpdateJob() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobUtil.checkForModelUpdate(appContext, managedModel, useWifi)
            registerJobServiceReceiver()
        }
        lastUpdateCheckedAt = System.currentTimeMillis()
    }

    fun launchDownloadModelJob() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobUtil.downloadModelVersion(managedModel, useWifi)
            registerJobServiceReceiver()
        }
        lastUpdateCheckedAt = System.currentTimeMillis()
    }

    companion object {
        private val TAG = ModelDownloadManager::class.java.simpleName
        // The time to wait between a check to the model version. (1 hr)
// You can change this if you want to test it quicker
        private val WAIT_BETWEEN_MODEL_VERSION_CHECKS = TimeUnit.SECONDS.toMillis(3600)
    }

}