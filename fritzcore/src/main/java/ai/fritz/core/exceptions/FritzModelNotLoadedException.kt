package ai.fritz.core.exceptions

/**
 * A runtime exception when trying to access a model that has not been downloaded
 * on the device.
 */
class FritzModelNotLoadedException : RuntimeException("Model is not loaded yet.")