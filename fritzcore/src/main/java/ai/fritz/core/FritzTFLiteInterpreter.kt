package ai.fritz.core

import ai.fritz.core.factories.ModelEventFactory
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Tensor
import java.io.IOException
import java.util.logging.Logger

/**
 * A TensorFlow Lite interpreter to manage and track model inference with Fritz.
 *
 *
 *
 * The FritzTFLite interpreter wraps around the TensorFlow Lite Interpreter class.
 * All methods should be modeled after those in TensorFlow Lite's Interpreter class.
 */
class FritzTFLiteInterpreter @JvmOverloads constructor(currentOnDeviceModel: FritzOnDeviceModel?, interpreterOptions: Interpreter.Options? = Interpreter.Options()) : FritzInterpreter<Interpreter?>(currentOnDeviceModel!!) {
    override var interpreter: Interpreter? = null
        public get() {
            return field
        }
        private set
    private var optionBuilder: TFLInterpreterOptionBuilder? = null

    constructor(currentOnDeviceModel: FritzOnDeviceModel?, optionBuilder: TFLInterpreterOptionBuilder) : this(currentOnDeviceModel, optionBuilder.buildInterpreterOptions()) {
        this.optionBuilder = optionBuilder
    }

    /**
     * Get the number of input tensors.
     *
     * @return the count
     */
    val inputTensorCount: Int
        get() = interpreter!!.inputTensorCount

    /**
     * Get the number of output tensors.
     *
     * @return the count
     */
    val outputTensorCount: Int
        get() = interpreter!!.outputTensorCount

    /**
     * Get the input tensor at the specific index.
     *
     * @return the tensor
     */
    fun getInputTensor(inputIndex: Int): Tensor {
        return interpreter!!.getInputTensor(inputIndex)
    }

    /**
     * Get the output tensor at the specific index.
     *
     * @return the tensor
     */
    fun getOutputTensor(outputIndex: Int): Tensor {
        return interpreter!!.getOutputTensor(outputIndex)
    }

    override fun checkRefreshInterpreter(newOnDeviceModel: FritzOnDeviceModel?) {
        if (!shouldRefreshInterpreter(onDeviceModel, newOnDeviceModel!!)) {
            return
        }
        try {
            val options = if (optionBuilder != null) optionBuilder!!.buildInterpreterOptions() else Interpreter.Options()
            val newInterpreter = Interpreter(ModelReader(newOnDeviceModel).readModelFile(), options)
            val previousOnDeviceModel = onDeviceModel
            onDeviceModel = newOnDeviceModel
            interpreter = newInterpreter
            previousOnDeviceModel.deleteModelFile()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    /**
     * Run model inference on the input and output methods.
     *
     *
     * The interpreter will record metrics on model execution.
     */
    fun run(input: Any?, output: Any?) {
        if (modelDownloadManager != null) {
            modelDownloadManager!!.checkForNewActiveVersion()
        }
        val start = System.currentTimeMillis()
        interpreter!!.run(input, output)
        Log.d(TAG, "Inference Time: " + (System.currentTimeMillis() - start))
        trackInferenceTime()
    }

    /**
     * Runs model inference for multiple inputs / outputs.
     *
     *
     * The interpreter will record metrics on model execution.
     *
     * @param inputs
     * @param outputs
     */
    fun runForMultipleInputsOutputs(inputs: Array<Any?>?, outputs: Map<Int?, Any?>?) {
        if (modelDownloadManager != null) {
            modelDownloadManager!!.checkForNewActiveVersion()
        }
        val start = System.currentTimeMillis()
        interpreter!!.runForMultipleInputsOutputs(inputs, outputs!!)
        Log.d(TAG, "Inference Time: " + (System.currentTimeMillis() - start))
        trackInferenceTime()
    }

    private fun trackInferenceTime() { // Only track the run methods
        val elapsed = lastNativeInferenceDurationNanoseconds ?: return
        val predictionTiming = ModelEventFactory.createPredictionTiming(onDeviceModel, elapsed)
        Fritz.sessionManager.track(predictionTiming)
    }

    /**
     * Resizes idx-th input of the native model to the given dims.
     *
     * @param idx  index
     * @param dims dimensions
     */
    fun resizeInput(idx: Int, dims: IntArray?) {
        interpreter!!.resizeInput(idx, dims)
    }

    /**
     * Gets index of an input given the op name of the input.
     *
     * @param opName operation name
     */
    fun getInputIndex(opName: String?): Int {
        return interpreter!!.getInputIndex(opName)
    }

    /**
     * Gets index of an output given the op name of the output.
     *
     * @param opName operation name
     */
    fun getOutputIndex(opName: String?): Int {
        return interpreter!!.getOutputIndex(opName)
    }

    /**
     * Release resources associated with the `Interpreter`.
     */
    fun close() {
        interpreter!!.close()
    }

    /**
     * Get the inference timing in nanoseconds.
     */
    val lastNativeInferenceDurationNanoseconds: Long
        get() = interpreter!!.lastNativeInferenceDurationNanoseconds

    companion object {
        private val TAG = FritzTFLiteInterpreter::class.java.simpleName
        private val logger = Logger.getLogger(
                FritzTFLiteInterpreter::class.java.simpleName)
    }

    init {
        try {
            interpreter = Interpreter(ModelReader(currentOnDeviceModel!!).readModelFile(), interpreterOptions)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}