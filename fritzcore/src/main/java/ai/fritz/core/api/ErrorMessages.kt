package ai.fritz.core.api

/**
 * @hide
 */
object ErrorMessages {
    private const val GETTING_STARTED_DOC_LINK = "https://docs.fritz.ai/get-started.html"
    /**
     * When there's a failure on the session settings request, return this message.
     *
     * @return a message telling them to check a couple
     */
    @JvmStatic
    val sessionSettingsFailureMessage: String
        get() = "Fritz session settings request failed. Check the following:\n" +
                "- You've set up your 'fritz_api_key' in your app/AndroidManifest.xml correctly.\n" +
                "- Your package id matches the app's package id that you set up in the Fritz webapp\n" +
                "For more details, please visit: " + GETTING_STARTED_DOC_LINK
}