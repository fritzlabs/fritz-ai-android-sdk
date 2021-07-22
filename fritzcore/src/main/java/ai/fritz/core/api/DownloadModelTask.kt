package ai.fritz.core.api

import android.os.AsyncTask
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Download a new model file to the device
 * @hide
 */
class DownloadModelTask(private val modelId: String, private val modelVersion: Int, private val appDirectory: File, private val listener: PostExecuteListener) : AsyncTask<String?, String?, String?>() {

    interface PostExecuteListener {
        fun onSuccess(absolutePath: String?)
        fun onFailure()
    }

    override fun doInBackground(vararg url: String?): String? {
        var outputStream: FileOutputStream? = null
        try {
            val modelURL = URL(url[0])
            val urlConnection = modelURL.openConnection() as HttpURLConnection
            val savedFile = createModelFileName()
            val modelFile = File(appDirectory, savedFile)
            // Resume download if model file already exists from an incomplete download attempt
            outputStream = if (modelFile.exists()) {
                urlConnection.setRequestProperty("Range", "bytes=" + modelFile.length() + "-")
                FileOutputStream(modelFile, true)
            } else {
                FileOutputStream(modelFile)
            }
            urlConnection.connect()
            val bufferedInputStream: InputStream = BufferedInputStream(urlConnection.inputStream, READ_BYTE_BUFFER_LENGTH)
            val buffer = ByteArray(READ_BYTE_BUFFER_LENGTH)
            var dataSize: Int
            while (bufferedInputStream.read(buffer).also { dataSize = it } != -1) {
                outputStream.write(buffer, 0, dataSize)
            }
            bufferedInputStream.close()
            outputStream.flush()
            outputStream.close()
            Log.d(TAG, "DOWNLOADED:" + modelFile.absolutePath + " SIZE OF: " + modelFile.length())
            return modelFile.absolutePath
        } catch (e: IOException) {
            Log.w(TAG, e.toString())
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    Log.w(TAG, "Unable to close output stream.")
                }
            }
        }
        return null
    }

    /**
     * Creates a file name for this model for internal storage.
     *
     * @return the name of the model file received from over the air.
     */
    private fun createModelFileName(): String {
        return modelId + "_v" + modelVersion + ".tflite"
    }

    override fun onPostExecute(absolutePathForFile: String?) {
        if (absolutePathForFile == null) {
            listener.onFailure()
            return
        }
        listener.onSuccess(absolutePathForFile)
    }

    companion object {
        private const val READ_BYTE_BUFFER_LENGTH = 8192
        private val TAG = DownloadModelTask::class.java.simpleName
    }

}