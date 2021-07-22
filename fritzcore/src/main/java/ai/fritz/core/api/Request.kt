package ai.fritz.core.api

import org.json.JSONObject
import java.net.URL

/**
 * Request Object
 * @hide
 */
class Request @JvmOverloads constructor(val url: URL, val payload: JSONObject? = null)