package ai.fritz.core

import ai.fritz.core.api.ApiClient
import ai.fritz.core.api.RequestHandler
import ai.fritz.core.api.Session
import ai.fritz.core.events.ModelEvent
import ai.fritz.core.events.ModelEventQueue
import android.app.Application
import android.content.Context
import android.util.Log
import org.json.JSONObject

class SessionManager(var appContext: Context, var session: Session, var apiClient: ApiClient) {
    var eventQueue: ModelEventQueue = ModelEventQueue(session)

    fun registerLifecycleCallbacks() {
        val app = appContext as Application
        app.registerActivityLifecycleCallbacks(FritzLifecycleCallbacks())
    }

    fun fetchSessionSettings() {
        Log.e(TAG, "The Fritz AI backend as been disabled.")
    }

    fun track(event: ModelEvent) {
        eventQueue.add(event)
    }

    fun recordAnnotation(event: ModelEvent, onSuccess: () -> Unit, onError: () -> Unit) {
        val handler: RequestHandler = object : RequestHandler {
            override fun onSuccess(response: JSONObject?) {
                onSuccess()
            }

            override fun onError(response: JSONObject?) {
                onError()
            }
        }
        Log.e(TAG, "The Fritz AI backend as been disabled.")
    }

    /**
     * Flush all events in the queue if there are any
     */
    fun flushEvents() {
        eventQueue.flush()
    }

    companion object {
        private val TAG = SessionManager::class.java.simpleName
    }
}