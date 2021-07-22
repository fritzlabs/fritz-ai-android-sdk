package ai.fritz.core

import ai.fritz.core.Fritz.appContext
import ai.fritz.core.exceptions.FritzModelNotLoadedException
import ai.fritz.core.utils.FritzModelManager.Companion.getActiveOnDeviceModel
import ai.fritz.core.utils.FritzModelManager.Companion.handleModelInitialized
import ai.fritz.core.utils.ModelDownloadManager
import ai.fritz.core.utils.PreferenceManager.getSavedModel
import android.os.Handler
import android.os.Looper

/**
 * Base class for the Fritz Interpreter.
 *
 * @param <T> - the type of interpreter Fritz wraps around (TFL, TFM)
</T> */
abstract class FritzInterpreter<T>(currentOnDeviceModel: FritzOnDeviceModel) {
    var onDeviceModel: FritzOnDeviceModel
    var modelDownloadManager: ModelDownloadManager?
    abstract val interpreter: T
    abstract fun checkRefreshInterpreter(newOnDeviceModel: FritzOnDeviceModel?)
    protected fun shouldRefreshInterpreter(currentOnDeviceModel: FritzOnDeviceModel, activeOnDeviceModel: FritzOnDeviceModel): Boolean { // If the versions are different
        return activeOnDeviceModel.modelVersion != currentOnDeviceModel.modelVersion
    }

    companion object {
        private val TAG = FritzInterpreter::class.java.simpleName
    }

    /**
     * Initialize the interpreter.
     *
     *
     * For GPU handling, tie the model download to the current thread that initialized the FritzInterpreter.
     * If you're using the GPU option, the predictor / interpreter must be initialized on a thread with a Looper (e.g Main Thread).
     * If not this will crash when trying to perform inference because the thread which the interpreter was initialized will be different
     * than the one that is calling "interpreter.run".
     *
     * @param currentOnDeviceModel
     */
    init {
        onDeviceModel = currentOnDeviceModel
        modelDownloadManager = null
    }
}