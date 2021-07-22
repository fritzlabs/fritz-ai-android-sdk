package ai.fritz.core.api

import android.util.Log
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection

/**
 * Generic GET request
 * @hide
 */
class GetRequestTask : BaseRequestTask {
    constructor(session: Session, handler: RequestHandler) : super(session, handler) {}
    constructor(session: Session, handler: RequestHandler, fatalErrorListener: FatalErrorListener) : super(session, handler, fatalErrorListener) {}

    override fun doInBackground(vararg request: Request?): Response {
        var urlConnection: HttpURLConnection? = null
        return try {
            urlConnection = request[0]?.url?.openConnection() as HttpURLConnection
            Log.d(TAG, "Api Request: " + request[0]?.url)
            setupHeaders(urlConnection!!)
            val `in`: InputStream = BufferedInputStream(urlConnection.inputStream)
            val responsePayload = readInputStream(`in`)
            Response(urlConnection.responseCode, responsePayload)
        } catch (e: IOException) {
            Log.w(TAG, "Api Request failed: " + e.message)
            handleApiError(urlConnection)
        } finally {
            urlConnection?.disconnect()
        }
    }

    companion object {
        private val TAG = GetRequestTask::class.java.simpleName
    }
}