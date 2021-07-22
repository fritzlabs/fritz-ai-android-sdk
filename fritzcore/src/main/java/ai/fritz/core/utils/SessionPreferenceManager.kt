package ai.fritz.core.utils

import ai.fritz.core.api.Session
import ai.fritz.core.api.SessionSettings
import android.content.Context

/**
 * Persists session data into shared preferences.
 *
 *
 * To hold the single source of truth for session info.
 *
 * @hide
 */
object SessionPreferenceManager {
    @JvmStatic
    fun createSession(context: Context, instanceId: String, appToken: String, userAgent: String): Session {
        val session = Session(instanceId, appToken, userAgent)
        PreferenceManager.saveSession(context, session)
        return session
    }

    @JvmStatic
    fun updateSessionSettings(updatedSettings: SessionSettings): Session? {
        val session = PreferenceManager.getSession() ?: return null
        session.settings = updatedSettings
        PreferenceManager.saveSession(session)
        return session
    }

    @JvmStatic
    fun updateSessionSettings(context: Context, updatedSettings: SessionSettings): Session? {
        val session = PreferenceManager.getSession(context) ?: return null
        session.settings = updatedSettings
        PreferenceManager.saveSession(context, session)
        return session
    }

    @JvmStatic
    fun getSession(): Session? {
        return PreferenceManager.getSession()
    }

    @JvmStatic
    fun getSession(context: Context): Session? {
        return PreferenceManager.getSession(context)
    }
}