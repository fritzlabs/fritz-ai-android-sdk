package ai.fritz.core.exceptions

/**
 * A runtime exception when no API key for Fritz is defined.
 */
class FritzNotInitializedException: RuntimeException("Fritz is not initialized. Make sure you've initialized your app with fritz_api_key: https://docs.fritz.ai/get-started.html#android")