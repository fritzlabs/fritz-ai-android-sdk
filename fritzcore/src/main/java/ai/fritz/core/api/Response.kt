package ai.fritz.core.api

import org.json.JSONObject


val NO_INTERNET_RESPONSE = Response(-1, JSONObject())

/**
 * Holds the response for parsing
 * @hide
 */
class Response(val statusCode: Int, val body: JSONObject) {

    val isSuccessful: Boolean
        get() = statusCode in 200..299

}