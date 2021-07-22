package ai.fritz.core.api

import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.util.zip.GZIPOutputStream

/**
 * For all POST requests
 *
 * @hide
 */
class PostRequestTask(session: Session, handler: RequestHandler) : BaseRequestTask(session, handler) {
    override fun doInBackground(vararg request: Request?): Response {
        var urlConnection: HttpURLConnection? = null
        val req = request[0]!!
        return try {
            Log.d(TAG, "Api Request: " + req.url)
            urlConnection = req.url.openConnection() as HttpURLConnection
            setupHeaders(urlConnection!!)
            val payloadString = req.payload.toString()
            val payloadBytes: ByteArray
            if (session.settings.isGzipTrackEvents) {
                payloadBytes = compress(payloadString)
                urlConnection.setRequestProperty("Content-Encoding", "gzip")
                urlConnection.setRequestProperty("Content-Length", payloadBytes.size.toString())
            } else {
                payloadBytes = payloadString.toByteArray(charset(BaseRequestTask.Companion.TEXT_ENCODING))
            }
            urlConnection.setRequestProperty("Content-Type", CONTENT_TYPE)
            urlConnection.doOutput = true
            urlConnection.requestMethod = "POST"
            // Setup the payload
            val wr = DataOutputStream(urlConnection.outputStream)
            wr.write(payloadBytes)
            wr.close()
            // Send the request
            val `in`: InputStream = BufferedInputStream(urlConnection.inputStream)
            val result = readInputStream(`in`)
            Response(urlConnection.responseCode, result)
        } catch (e: IOException) {
            Log.w(TAG, "Api Request failed: " + e.message)
            handleApiError(urlConnection)
        } finally {
            urlConnection?.disconnect()
        }

    }

    companion object {
        private val TAG = PostRequestTask::class.java.simpleName
        private const val CONTENT_TYPE = "application/json"

        @Throws(IOException::class)
        private fun compress(string: String): ByteArray {
            val os = ByteArrayOutputStream(string.length)
            val gos = GZIPOutputStream(os)
            gos.write(string.toByteArray(charset(BaseRequestTask.Companion.TEXT_ENCODING)))
            gos.close()
            val compressed = os.toByteArray()
            os.close()
            return compressed
        }
    }
}