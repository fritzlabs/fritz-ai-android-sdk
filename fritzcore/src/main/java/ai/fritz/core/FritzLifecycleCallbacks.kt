package ai.fritz.core

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

/**
 * Lifecycle callbacks for Fritz
 * @hide
 */
class FritzLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        Fritz.sessionManager.fetchSessionSettings()
    }

    override fun onActivityPaused(activity: Activity) {
        Log.d(TAG, "Flushing events on app background")
        Fritz.sessionManager.flushEvents()
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle?) {}
    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        private val TAG = FritzLifecycleCallbacks::class.java.simpleName
    }
}