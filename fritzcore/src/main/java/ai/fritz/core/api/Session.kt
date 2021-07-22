package ai.fritz.core.api

import org.json.JSONException
import org.json.JSONObject

/**
 * The session is set for the ApiClient.
 *
 *
 * We send this information along for each request in the header.
 *
 * @hide
 */
class Session {
    var instanceId: String
        private set
    var appToken: String
        private set
    var userAgent: String
        private set
    var settings: SessionSettings

    constructor(instanceId: String, appToken: String, userAgent: String) {
        this.instanceId = instanceId
        this.appToken = appToken
        this.userAgent = userAgent
        settings = SessionSettings.Companion.createDefault()
    }

    constructor(obj: JSONObject) {
        instanceId = obj.getString(INSTANCE_ID_KEY)
        appToken = obj.getString(APP_TOKEN_KEY)
        userAgent = obj.getString(USER_AGENT_KEY)
        settings = SessionSettings.Companion.fromSharedPreferences(obj.getJSONObject(SETTINGS_KEY))
    }

    val isApiEnabled: Boolean
        get() = settings.isEnabledApiRequests

    @Throws(JSONException::class)
    fun toJson(): JSONObject {
        val `object` = JSONObject()
        `object`.put(INSTANCE_ID_KEY, instanceId)
        `object`.put(APP_TOKEN_KEY, appToken)
        `object`.put(USER_AGENT_KEY, userAgent)
        `object`.put(SETTINGS_KEY, settings.toJson())
        return `object`
    }

    /**
     * Checks if the sessions are equal.
     *
     * @param other The other session
     * @return true if equal, false otherwise
     * @hide
     */
    override fun equals(other: Any?): Boolean {
        if (other is Session) {
            val otherSession = other
            if (otherSession.instanceId == instanceId && otherSession.appToken == appToken && otherSession.userAgent == userAgent && otherSession.settings == settings) {
                return true
            }
        }
        return false
    }

    companion object {
        private const val INSTANCE_ID_KEY = "instance_id"
        private const val APP_TOKEN_KEY = "app_token"
        private const val USER_AGENT_KEY = "user_agent"
        private const val SETTINGS_KEY = "settings_key"
    }
}