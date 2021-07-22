package ai.fritz.core.api

import ai.fritz.core.constants.ApiHeaders
import android.os.AsyncTask
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import javax.net.ssl.HttpsURLConnection

/**
 * All request tasks should extend from this one.
 *
 * @hide
 */
abstract class BaseRequestTask : AsyncTask<Request?, Void?, Response?> {
    protected var session: Session
    protected var handler: RequestHandler
    private var fatalErrorListener: FatalErrorListener

    constructor(session: Session, handler: RequestHandler) {
        this.handler = handler
        this.session = session
        fatalErrorListener = object : FatalErrorListener {
            override fun onFatalError(message: String) {
                throw FatalAuthException("\n\n" +
                        message + "\n" +
                        "Please check your applicationId and API Key are correctly registered in your Fritz account.\n" +
                        "https://docs.fritz.ai/quickstart.html\n\n" +
                        "To fix the issue, please visit our support forum https://support.fritz.ai/t/my-app-won-t-build-because-bundle-identifier-application-id-does-not-match-api-key/37 .\n\n")
            }
        }
    }

    constructor(session: Session, handler: RequestHandler, fatalErrorListener: FatalErrorListener) {
        this.handler = handler
        this.session = session
        this.fatalErrorListener = fatalErrorListener
    }

    override fun onPostExecute(response: Response?) {
        super.onPostExecute(response)
        if (response == null) {
            handler.onError(JSONObject())
            return
        }
        val withStatusCode = response.body
        try {
            withStatusCode.put(STATUS_CODE_KEY, response.statusCode)
        } catch (e: JSONException) {
            Log.w(TAG, "JSONException with status code")
            throw RuntimeException(e)
        }
        if (response.isSuccessful) {
            handler.onSuccess(withStatusCode)
        } else {
            handleFatalErrorCallback(response)
            handler.onError(withStatusCode)
        }
    }

    private fun handleFatalErrorCallback(response: Response) {
        val body = response.body
        var isFatal = false
        var message = ""
        try {
            if (body!!.has(IS_FATAL_KEY)) {
                isFatal = body.getBoolean(IS_FATAL_KEY)
            }
            if (body.has(MESSAGE_KEY)) {
                message = body.getString(MESSAGE_KEY)
            }
        } catch (jsonException: JSONException) {
            Log.e(TAG, jsonException.toString())
        }
        if (isFatal) {
            fatalErrorListener.onFatalError(message)
        }
    }

    override fun onCancelled(response: Response?) {
        super.onCancelled(response)
        if (response == null) {
            handler.onError(JSONObject())
            return
        }
        handler.onError(response.body)
    }

    protected fun handleApiError(urlConnection: HttpURLConnection?): Response {
        if (urlConnection != null) {
            val errorStream = urlConnection.errorStream
            if (errorStream != null) {
                try {
                    val responseCode = urlConnection.responseCode
                    if (responseCode != HttpsURLConnection.HTTP_INTERNAL_ERROR) {
                        val errorPayload = readInputStream(errorStream)
                        // pass the error payload and response code back.
                        return Response(urlConnection.responseCode, errorPayload)
                    }
                    return Response(urlConnection.responseCode, JSONObject())
                } catch (errorReadException: IOException) {
                    Log.e(TAG, "Cannot read error message: " + errorReadException.message)
                }
            }
        }
        return NO_INTERNET_RESPONSE
    }

    protected fun setupHeaders(urlConnection: HttpURLConnection) {
        urlConnection.setRequestProperty(ApiHeaders.USER_AGENT, session.userAgent)
        urlConnection.setRequestProperty(ApiHeaders.FRITZ_APP_TOKEN, session.appToken)
        urlConnection.setRequestProperty(ApiHeaders.FRITZ_INSTANCE_ID, session.instanceId)
    }

    @Throws(IOException::class)
    protected fun readInputStream(`in`: InputStream?): JSONObject {
        val reader = BufferedReader(InputStreamReader(`in`, TEXT_ENCODING))
        val result = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            result.append(line)
        }
        reader.close()
        try {
            return JSONObject(result.toString())
        } catch (e: JSONException) {
            Log.e(TAG, e.message)
        }
        return JSONObject()
    }

    companion object {
        const val STATUS_CODE_KEY = "status_code"
        const val TEXT_ENCODING = "UTF-8"
        private const val IS_FATAL_KEY = "is_fatal"
        private const val MESSAGE_KEY = "message"
        private val TAG = BaseRequestTask::class.java.simpleName
    }
}