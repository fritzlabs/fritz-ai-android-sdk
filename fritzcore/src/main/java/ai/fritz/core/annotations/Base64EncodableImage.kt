package ai.fritz.core.annotations

import android.util.Size

interface Base64EncodableImage {
    fun encodedInput(): String
    fun encodedSize(): Size
    fun encodedImageFormat(): String
}