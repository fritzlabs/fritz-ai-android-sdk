package ai.fritz.core

import ai.fritz.core.Fritz.appContext
import ai.fritz.core.constants.ModelEventName
import ai.fritz.core.factories.ModelEventFactory
import org.cryptonode.jncryptor.AES256JNCryptor
import org.cryptonode.jncryptor.CryptorException
import java.io.*
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets

/**
 * For reading an on device model.
 *
 * @hide
 */
class ModelReader(private val onDeviceModel: FritzOnDeviceModel) {
    /**
     * Load the specified model file and return a MappedByteBuffer.
     *
     * @return a MappedByteBuffer of the loaded file
     * @throws IOException if the file can't be loaded.
     * @hide
     */
    @Throws(IOException::class)
    fun readModelFile(): MappedByteBuffer {
        val context = appContext
        val modelToRead = onDeviceModel
        val modelPath = modelToRead.modelPath
        // If the file is stored in assets, load it
        val hasAssetPrefix = modelPath.startsWith(ANDROID_ASSET_ROOT)
        if (hasAssetPrefix) {
            val pathName = modelPath.split(ANDROID_ASSET_ROOT).toTypedArray()[1]
            val fileDescriptor = context!!.assets.openFd(pathName)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }
        // Use the regular model path.
        val modelFile = File(modelPath)
        val inputStream = FileInputStream(modelFile)
        val fileChannel = inputStream.channel
        val startOffset: Long = 0
        val declaredLength = modelFile.length()
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun fileToBytes(inputStream: InputStream): ByteArray {
        return try { //init array with file length
            val bytesArray = ByteArray(inputStream.available())
            inputStream.read(bytesArray) //read file into bytes[]
            bytesArray
        } catch (e: FileNotFoundException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private const val ANDROID_ASSET_ROOT = "file:///android_asset/"
    }

}