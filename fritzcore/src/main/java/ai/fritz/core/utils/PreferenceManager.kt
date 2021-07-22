package ai.fritz.core.utils

import ai.fritz.core.Fritz
import ai.fritz.core.FritzOnDeviceModel
import ai.fritz.core.FritzOnDeviceModel.Companion.buildFromJson
import ai.fritz.core.api.Session
import ai.fritz.core.constants.SPKeys
import android.content.Context
import android.content.SharedPreferences
import org.json.JSONException
import org.json.JSONObject

/**
 * Manages persisted data to shared preferences
 *
 * @hide
 */
object PreferenceManager {
    @JvmStatic
    fun getSavedModel(context: Context, modelId: String): FritzOnDeviceModel? {
        val modelSettingsStr = getString(context, SPKeys.getModelKeyById(modelId))
                ?: return null
        return try {
            buildFromJson(JSONObject(modelSettingsStr))
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun saveModel(context: Context, fritzOnDeviceModel: FritzOnDeviceModel?) {
        val onDeviceModelJson = fritzOnDeviceModel!!.toJson()
        // Save the custom model
        putString(context, SPKeys.getModelKey(fritzOnDeviceModel), onDeviceModelJson.toString())
        // Save model version for quick access
        putInt(context, SPKeys.getActiveVersionForModelKey(fritzOnDeviceModel), fritzOnDeviceModel.modelVersion)
    }

    @JvmStatic
    fun hasTrackedModelVersionInstall(context: Context, onDeviceModel: FritzOnDeviceModel): Boolean {
        val key = SPKeys.getHasTrackedModelVersionKey(onDeviceModel.modelId, onDeviceModel.modelVersion)
        return getBoolean(context, key)
    }

    @JvmStatic
    fun saveHasTrackedModelVersionInstall(context: Context, onDeviceModel: FritzOnDeviceModel, value: Boolean) {
        val key = SPKeys.getHasTrackedModelVersionKey(onDeviceModel.modelId, onDeviceModel.modelVersion)
        putBoolean(context, key, value)
    }

    @JvmStatic
    fun saveSession(context: Context, session: Session) {
        try {
            putString(context, SPKeys.FRITZ_SESSION, session.toJson().toString())
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun saveSession(session: Session) {
        saveSession(Fritz.appContext, session)
    }

    @JvmStatic
    fun getSession(): Session? {
        return getSession(Fritz.appContext)
    }

    @JvmStatic
    fun getSession(context: Context): Session? {
        val fritzSessionStr = getString(context, SPKeys.FRITZ_SESSION) ?: return null
        return try {
            return Session(JSONObject(fritzSessionStr))
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun clearAll(context: Context) {
        getSharedPreference(context).edit().clear().apply()
    }

    @JvmStatic
    fun getString(context: Context, key: String?): String? {
        val preferences = getSharedPreference(context)
        return preferences.getString(key, null)
    }

    @JvmStatic
    fun putString(context: Context, key: String?, value: String?) {
        getSharedPreference(context).edit().putString(key, value).apply()
    }

    @JvmStatic
    fun putInt(context: Context, key: String?, value: Int) {
        getSharedPreference(context).edit().putInt(key, value).apply()
    }

    @JvmStatic
    fun getInt(context: Context, key: String?): Int {
        return getSharedPreference(context).getInt(key, -1)
    }

    @JvmStatic
    fun putBoolean(context: Context, key: String?, value: Boolean) {
        getSharedPreference(context).edit().putBoolean(key, value).apply()
    }

    @JvmStatic
    fun getBoolean(context: Context, key: String?): Boolean {
        return getSharedPreference(context).getBoolean(key, false)
    }

    @JvmStatic
    private fun getSharedPreference(context: Context): SharedPreferences {
        val packageName = context!!.packageName
        return context.getSharedPreferences(packageName + SPKeys.BUNDLE_ID, Context.MODE_PRIVATE)
    }
}