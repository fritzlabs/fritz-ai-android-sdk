package ai.fritz.vision


import ai.fritz.core.InputTensor
import ai.fritz.vision.base.PreprocessParams
import android.util.Size

const val DEFAULT_HEIGHT_IDX: Int = 1
const val DEFAULT_WIDTH_IDX: Int = 2


/**
 * Image input tensor.
 *
 * Includes the preprocessing for a Fritz vision image to prepare it for the model.
 */
class ImageInputTensor(name: String, tensorIndex: Int) : InputTensor(name, tensorIndex) {

    fun getImageDimensions(): Size? {
        val inputShape = tensor.shape()
        val inputHeight = inputShape[DEFAULT_HEIGHT_IDX]
        val inputWidth = inputShape[DEFAULT_WIDTH_IDX]
        return Size(inputWidth, inputHeight)
    }

    fun preprocess(visionImage: FritzVisionImage) {
        preprocess(visionImage, null)
    }

    fun preprocess(visionImage: FritzVisionImage, preprocessParams: PreprocessParams? = null) {
        val preparedImage = visionImage.prepareBytes(getImageDimensions())

        if (is8BitQuantized()) {
            loadBufferForQuantizedInput(preparedImage)
        } else {
            loadBufferForFloatInput(preparedImage, preprocessParams)
        }
    }

    /**
     * Input buffer is byte quantized UINT8.
     *
     * @param modelInputBuffer
     * @param visionImage
     */
    protected fun loadBufferForQuantizedInput(preparedImage: ByteImage) {
        buffer.rewind()
        val copyImageBuffer = preparedImage.copyOfImageData
        var step = 0
        for (color in copyImageBuffer) {
            // Skip alpha values
            if (++step % 4 == 0) {
                continue
            }
            buffer.put(color)
        }
    }

    /**
     * Input buffer will contain the the processed pixel values.
     *
     * @param preparedImage
     * @param preprocessParams
     */
    protected fun loadBufferForFloatInput(preparedImage: ByteImage, preprocessParams: PreprocessParams? = null) {
        buffer.rewind()
        val copyBuffer = preparedImage.copyOfImageData
        var step = 0
        for (color in copyBuffer) {
            // Skip alpha values
            if (++step % 4 == 0) {
                continue
            }
            if (preprocessParams == null) {
                buffer.putFloat((color.toInt() and 0xFF).toFloat())
            } else {
                buffer.putFloat(preprocessParams.normalize((color.toInt() and 0xFF).toFloat()))
            }
        }
    }
}