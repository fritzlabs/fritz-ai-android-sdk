package ai.fritz.core.events

import ai.fritz.core.Fritz
import ai.fritz.core.api.RequestHandler
import ai.fritz.core.api.Session
import ai.fritz.core.utils.SessionPreferenceManager
import android.util.Log
import org.json.JSONObject
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The queue for model events.
 *
 * @hide
 */
class ModelEventQueue(var session: Session) {
    private val queue: Queue<ModelEvent>
    private var eventsToSend: MutableList<ModelEvent>
    private val isPostingEvents: AtomicBoolean
    private val timer: Timer
    private val task: TimerTask
    private val handler: RequestHandler
    fun add(modelEvent: ModelEvent) {
        val session = SessionPreferenceManager.getSession()
        val eventBlacklist = session!!.settings.eventBlacklist
        // skip the events in the blacklist
        if (eventBlacklist!!.contains(modelEvent.name)) {
            Log.d(TAG, modelEvent.name + " is in the event blacklist. Skipping it.")
            return
        }
        // Add if not in the blacklist
        queue.add(modelEvent)
        // If we reached the threshold, flush the queue.
        if (queue.size >= session.settings.trackRequestBatchSize) {
            flush()
        }
    }

    operator fun contains(modelEvent: ModelEvent?): Boolean {
        return queue.contains(modelEvent)
    }

    private val isEmpty: Boolean
        private get() = queue.size == 0 && eventsToSend.size == 0

    /**
     * Flush the items in a queue and move it over to an intermediate list that we send off.
     * If there's an error, items remain in that list until a successful send.
     */
    fun flush() {
        if (isEmpty) {
            return
        }
        // Ensure that only one request is sent at a time. If one is in progress, don't do anything.
        if (isPostingEvents.compareAndSet(false, true)) { // Pop events from the queue to send
            for (i in queue.indices.reversed()) {
                val modelEvent = queue.remove()
                eventsToSend.add(modelEvent)
            }
            // Drop the oldest events (at the front of eventsToSend). The queue
// always has newer events than eventsToSend.
// (oldest events are at the front of eventsToSend)
            if (eventsToSend.size > MAX_EVENTS_TO_SEND) {
                eventsToSend = eventsToSend.subList(eventsToSend.size - MAX_EVENTS_TO_SEND, eventsToSend.size)
                Log.w(TAG, "Max event size reached. Dropping the oldest events.")
            }

            Fritz.sessionManager.apiClient.batchTracking(eventsToSend, handler)
        }
    }

    fun clearAll() {
        queue.clear()
        eventsToSend.clear()
        isPostingEvents.set(false)
    }

    fun overrideIsPostingEvents(override: Boolean) {
        isPostingEvents.set(override)
    }

    private fun schedulerStart() {
        val queueFlushTime = session.settings.batchFlushInterval
        timer.scheduleAtFixedRate(task, queueFlushTime, queueFlushTime)
    }

    private fun schedulerStop() {
        timer.cancel()
    }

    companion object {
        private val TAG = ModelEventQueue::class.java.simpleName
        const val MAX_EVENTS_TO_SEND = 1000
    }

    init {
        queue = LinkedBlockingQueue()
        eventsToSend = ArrayList()
        timer = Timer()
        isPostingEvents = AtomicBoolean(false)
        // Setup the request handler
        handler = object : RequestHandler {
            override fun onSuccess(response: JSONObject?) {
                eventsToSend.clear()
                isPostingEvents.compareAndSet(true, false)
            }

            override fun onError(response: JSONObject?) {
                isPostingEvents.compareAndSet(true, false)
            }
        }
        // Setup the timer task
        task = object : TimerTask() {
            override fun run() {
                flush()
            }
        }
        // Only started once since
        schedulerStart()
    }
}