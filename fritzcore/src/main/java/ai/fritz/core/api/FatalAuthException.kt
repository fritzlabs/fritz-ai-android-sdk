package ai.fritz.core.api

class FatalAuthException(message: String?) : RuntimeException(message) {
    override fun fillInStackTrace(): Throwable {
        return this
    }
}