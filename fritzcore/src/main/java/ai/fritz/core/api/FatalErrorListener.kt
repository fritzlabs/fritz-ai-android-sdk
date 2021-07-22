package ai.fritz.core.api

interface FatalErrorListener {
    fun onFatalError(message: String)
}