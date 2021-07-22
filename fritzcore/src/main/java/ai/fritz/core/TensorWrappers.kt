package ai.fritz.core

import android.util.Size
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Tensor
import java.nio.ByteBuffer
import java.nio.ByteOrder

const val DEFAULT_HEIGHT_IDX: Int = 1
const val DEFAULT_WIDTH_IDX: Int = 2

/**
 * Base tensor wrapper
 */
open class BaseTensor(val name: String, val tensorIndex: Int) {
    lateinit var tensor: Tensor
    lateinit var buffer: ByteBuffer

    fun rewind() {
        buffer.rewind()
    }

    fun is8BitQuantized(): Boolean {
        return tensor.dataType() === DataType.UINT8;
    }

    fun isDataTypeFloat(): Boolean {
        return tensor.dataType() === DataType.FLOAT32;
    }
}

/**
 * A tensor wrapper with convenience functions for inputs
 */
open class InputTensor(name: String, tensorIndex: Int) : BaseTensor(name, tensorIndex) {

    fun setupInputBuffer(interpreter: FritzTFLiteInterpreter) {
        tensor = interpreter.getInputTensor(tensorIndex)
        val dataType = tensor.dataType()
        buffer = ByteBuffer.allocateDirect(dataType.byteSize() * tensor.numElements())
        buffer.order(ByteOrder.nativeOrder())
    }
}

/**
 * A tensor wrapper with convenience functions for outputs
 */
open class OutputTensor(name: String, tensorIndex: Int) : BaseTensor(name, tensorIndex) {

    fun setupOutputBuffer(interpreter: FritzTFLiteInterpreter) {
        tensor = interpreter.getOutputTensor(tensorIndex)
        val dataType = tensor.dataType()
        buffer = ByteBuffer.allocateDirect(dataType.byteSize() * tensor.numElements())
        buffer.order(ByteOrder.nativeOrder())
    }

    /**
     * Get the byte at the index
     * @param index: the index
     */
    fun getByte(index: Int): Byte {
        val byteSize = tensor.dataType().byteSize()
        return buffer.get(byteSize * index)
    }

    /**
     * Get the float at the index.
     *
     * Note: The index is multiplied by the byte size of the float for buffer access
     * buffer.getFloat(byteSize * index)
     *
     * @param index: the index
     */
    fun getFloat(index: Int): Float {
        val byteSize = tensor.dataType().byteSize()
        return buffer.getFloat(byteSize * index)
    }

    /**
     * Get float at buffer[row][col]
     *
     * Note: buffer.getFloat(byteSize * (numColumns * row + column))
     *
     * @param row: the row to access
     * @param col: the col to access
     */
    fun getFloat2D(row: Int, column: Int): Float {
        val shape = tensor.shape()
        val numColumns = shape[shape.size - 1]
        val byteSize = tensor.dataType().byteSize()
        return buffer.getFloat(byteSize * (numColumns * row + column))
    }

    /**
     * Get the float at buffer[row][col][channel]
     *
     * Note: buffer.getFloat(byteSize * (row * numChannels * numColumns + column * numChannels + channel))
     * @param row: the row to access
     * @param col: the col to access
     * @param channel: the channel to access
     */
    fun getFloat3D(row: Int, column: Int, channel: Int): Float {
        val shape = tensor.shape()
        val numChannels = shape[shape.size - 1]
        val numColumns = shape[shape.size - 2]
        val byteSize = tensor.dataType().byteSize()
        return buffer.getFloat(byteSize * (row * numChannels * numColumns + column * numChannels + channel))
    }

    /**
     * Get the bounds for the output (optional)
     *
     * If the output is in the format (batch)(height)(width)(channels)
     */
    fun getBounds(): Size {
        val inputShape = tensor.shape()
        val inputHeight = inputShape[DEFAULT_HEIGHT_IDX]
        val inputWidth = inputShape[DEFAULT_WIDTH_IDX]
        return Size(inputWidth, inputHeight)
    }
}