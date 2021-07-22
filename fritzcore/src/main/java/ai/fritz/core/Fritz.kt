package ai.fritz.core

import ai.fritz.core.api.ApiClient
import ai.fritz.core.api.RequestHandler
import ai.fritz.core.api.Session
import ai.fritz.core.utils.FritzModelManager
import ai.fritz.core.utils.SessionPreferenceManager
import ai.fritz.core.utils.UserAgentUtil
import ai.fritz.listeners.DownloadTaggedModelsListener
import ai.fritz.listeners.SearchModelTagsListener
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Fritz is the core class to set up the SDK.
 *
 *
 *
 * You must initialize the SDK before using any dependant libraries. You may do so by calling
 * [.configure], which searches for fritz_app_token defined in the AndroidManifest.
 *
 *
 *
 * For more information, visit
 * [Getting started](https://docs.fritz.ai/get-started.html#android)
 *
 */
object Fritz {
    private val TAG = Fritz::class.java.simpleName
    private const val API_KEY = "fritz_api_key"
    private const val MODEL_ID_KEY = "id"

    @JvmStatic
    lateinit var sessionManager: SessionManager

    @JvmStatic
    lateinit var appContext: Context

    @JvmStatic
    lateinit var session: Session

    @JvmStatic
    val enableOTAModelUpdates: Boolean = false

    /**
     * Initialize the SDK with an app token.
     *
     * @param context
     * @param appToken
     */
    /**
     * Initializes the Fritz SDK with the `fritz_app_token` found in the AndroidManifest.xml file.
     *
     *
     *
     * Call `Fritz.configure(context); ` from the MainActivity or Application's onCreate method.
     *
     *
     *
     * This allows you to integrate ready-to-use features or monitor your existing models running on mobile.
     *
     * @param context the calling context (application, activity)
     */
    @JvmStatic
    @JvmOverloads
    fun configure(context: Context, appToken: String? = null) {
        session = SessionPreferenceManager.getSession(context)
                ?: intializeSession(context.applicationContext, appToken)
        appContext = context.applicationContext
        var apiClient = ApiClient(session, appContext.getString(R.string.api_base) + "/sdk/v1")
        sessionManager = SessionManager(context.applicationContext, session, apiClient)
        sessionManager.registerLifecycleCallbacks()
        sessionManager.fetchSessionSettings()
    }

    @JvmStatic
    @JvmOverloads
    fun configure(sessionManager: SessionManager) {
        session = sessionManager.session
        appContext = sessionManager.appContext
        this.sessionManager = sessionManager
        sessionManager.registerLifecycleCallbacks()
        sessionManager.fetchSessionSettings()
    }

    @JvmStatic
    fun intializeSession(context: Context, appKeyFromArgs: String?): Session {
        // Generate and save the instance id if it hasn't already been set.
        // Otherwise use the old instance id.
        val instanceId = generateInstanceId()
        val packageManager = context.packageManager
        // Setup the app token. Look for it in the AndroidManifest first.
        var apiKey: String?
        var appName: String?
        val pInfo: PackageInfo
        try {
            val ai = packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            pInfo = packageManager.getPackageInfo(context.packageName, 0)
            appName = packageManager.getApplicationLabel(ai) as String
            apiKey = appKeyFromArgs ?: searchForApiKey(context, ai.metaData)
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException("Fritz initialization failed. Package name not found")
        }

        val versionName = pInfo.versionName
        val versionCode = pInfo.versionCode
        val packageName = pInfo.packageName

        // Init the api client with these values
        val userAgent = UserAgentUtil.create(appName, packageName, versionName, versionCode)
        return SessionPreferenceManager.createSession(context, instanceId, apiKey, userAgent)

    }

    /**
     * Fetch all available models by tag.
     *
     * @param tag - the name of the tag associated with the model.
     * @param listener - a callback for handling the request.
     */
    @JvmStatic
    fun fetchManagedModelsByTag(tag: String, listener: SearchModelTagsListener) {
        fetchManagedModelsByTags(arrayOf(tag), listener)
    }

    /**
     * Fetch all available models that contain all listed tags.
     *
     * @param tags - the tags that the model must contain.
     * @param listener - a callback for handling the request.
     */
    @JvmStatic
    fun fetchManagedModelsByTags(tags: Array<String>, listener: SearchModelTagsListener) {
        Log.e(TAG, "The Fritz AI backend as been disabled.")
    }

    /**
     * Download available models with the given tag.
     *
     * @param tag - the tag that the model must contain.
     * @param downloadTaggedModelsListener - a callback to handle when all models are downloaded onto the device.
     */
    @JvmStatic
    fun loadOnDeviceModelsByTag(tag: String, downloadTaggedModelsListener: DownloadTaggedModelsListener) {
        loadOnDeviceModelsByTags(arrayOf(tag), downloadTaggedModelsListener)
    }

    /**
     * Download available models that contain all of the provided tags.
     *
     * @param tags - the tags that the model must contain.
     * @param downloadTaggedModelsListener - a callback to handle when all models are downloaded onto the device.
     */
    @JvmStatic
    fun loadOnDeviceModelsByTags(tags: Array<String>, downloadTaggedModelsListener: DownloadTaggedModelsListener) {
        fetchManagedModelsByTags(tags, object : SearchModelTagsListener {
            override fun onCompleted(managedModelVersions: MutableList<FritzManagedModel>) {
                downloadModels(managedModelVersions, downloadTaggedModelsListener)
            }

            override fun onError() {
                downloadTaggedModelsListener.onError()
            }
        })
    }

    private fun downloadModels(managedModels: MutableList<FritzManagedModel>, downloadTaggedModelsListener: DownloadTaggedModelsListener) {
    }

    private fun downloadModel(modelsToDownload: Queue<FritzManagedModel>, loadedOnDeviceModels: MutableList<FritzOnDeviceModel>, downloadTaggedModelsListener: DownloadTaggedModelsListener) {
    }

    private fun searchForApiKey(context: Context, metadata: Bundle?): String { // check the metadata in android resources first
        var apiKey = getApiKeyFromMetadata(metadata)
        if (apiKey != null) {
            return apiKey
        }
        // if not found from metadata, fall back to searching in resources
        val packageName = context.packageName
        val id = context.resources?.getIdentifier(API_KEY, "string", packageName)
        // Resource was not found

        if (id == 0 || id == null) {
            throw RuntimeException("Fritz is not properly initialized. Please check that your Api Key is defined.")
        }
        return context.getString(id)
    }

    // Resource was not found
    private val apiKeyFromResources: String?
        private get() {
            val packageName = appContext?.packageName
            val id = appContext?.resources?.getIdentifier(API_KEY, "string", packageName)
                    ?: return null
            // Resource was not found
            return if (id == 0) {
                null
            } else appContext?.getString(id)
        }

    private fun getApiKeyFromMetadata(metadata: Bundle?): String? { // If no metadata (we expect the app token to be there), bail early
        return metadata?.getString(API_KEY, null)
    }

    private fun generateInstanceId(): String {
        return UUID.randomUUID().toString()
    }
}