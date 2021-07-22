/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.fritz.core.utils

import android.graphics.*

/**
 * Provides static functions to decode bitmaps at the optimal size
 */
object BitmapUtils {
    private val TAG = BitmapUtils::class.java.simpleName
    /**
     * Crop a center square in the image.
     *
     *
     * Uses the larger of the height or width to create the cropped square image.
     * *
     *
     * @return Return the newly cropped bitmap (centered)
     */
    fun centerCropSquare(bitmap: Bitmap?): Bitmap? {
        var bitmap = bitmap
        val imgSize = Math.min(bitmap!!.width, bitmap.height)
        bitmap = centerCrop(bitmap, imgSize, imgSize)
        return bitmap
    }

    /**
     * Resize the Bitmap for the underlying model.
     *
     *
     * Warning: this may change the aspect ratio of the image. If you'd like to maintain aspect,
     * use [.scale]
     *
     * @param width
     * @param height
     * @return the newly resized bitmap.
     */
    @JvmStatic
    fun resize(bitmap: Bitmap?, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap!!, width, height, false)
    }

    /**
     * Rotate the bitmap for the model.
     *
     * @param degrees
     */
    @JvmStatic
    fun rotate(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Scale the image while maintaining aspect ratio.
     *
     * @param targetWidth
     * @param targetHeight
     */
    fun scale(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val matrix = Matrix()
        // maintain the aspect ratio
        val scaleFactorWidth = targetWidth.toFloat() / bitmap.width
        val scaleFactorHeight = targetHeight.toFloat() / bitmap.height
        val maxScale = Math.max(scaleFactorWidth, scaleFactorHeight)
        matrix.postScale(maxScale, maxScale)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
    }

    /**
     * Draw the image onto a canvas.
     *
     * @param canvas
     */
    fun drawOnCanvas(bitmap: Bitmap?, canvas: Canvas) {
        canvas.drawBitmap(bitmap!!, Matrix(), Paint())
    }

    /**
     * Decode an image into a Bitmap, using sub-sampling if the hinted dimensions call for it.
     * Does not crop to fit the hinted dimensions.
     *
     * @param src an encoded image
     * @param w   hint width in px
     * @param h   hint height in px
     * @return a decoded Bitmap that is not exactly sized to the hinted dimensions.
     */
    fun decodeByteArray(src: ByteArray, w: Int, h: Int): Bitmap? {
        return try { // calculate sample size based on w/h
            val opts = BitmapFactory.Options()
            opts.inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(src, 0, src.size, opts)
            if (opts.mCancel || opts.outWidth == -1 || opts.outHeight == -1) {
                return null
            }
            opts.inSampleSize = Math.min(opts.outWidth / w, opts.outHeight / h)
            opts.inJustDecodeBounds = false
            BitmapFactory.decodeByteArray(src, 0, src.size, opts)
        } catch (t: Throwable) {
            null
        }
    }

    /**
     * Decode an image into a Bitmap, using sub-sampling if the desired dimensions call for it.
     *
     * @param src an encoded image
     * @param w   desired width in px
     * @param h   desired height in px
     * @return an exactly-sized decoded Bitmap that is center-cropped.
     */
    fun decodeByteArrayWithCenterCrop(src: ByteArray, w: Int, h: Int): Bitmap? {
        return try {
            val decoded = decodeByteArray(src, w, h)
            centerCrop(decoded, w, h)
        } catch (t: Throwable) {
            null
        }
    }

    /**
     * Returns a new Bitmap copy with a center-crop effect a la
     * scaling is necessary.
     *
     * @param src original bitmap of any size
     * @param w   desired width in px
     * @param h   desired height in px
     * @return a copy of src conforming to the given width and height, or src itself if it already
     * matches the given width and height
     */
    fun centerCrop(src: Bitmap?, w: Int, h: Int): Bitmap? {
        return crop(src, w, h, 0.5f, 0.5f)
    }

    /**
     * Returns a new Bitmap copy with a crop effect depending on the crop anchor given. 0.5f is like
     * The crop anchor will be be nudged
     * so the entire cropped bitmap will fit inside the src. May return the input bitmap if no
     * scaling is necessary.
     *
     *
     *
     *
     * Example of changing verticalCenterPercent:
     * _________            _________
     * |         |          |         |
     * |         |          |_________|
     * |         |          |         |/___0.3f
     * |---------|          |_________|\
     * |         |<---0.5f  |         |
     * |---------|          |         |
     * |         |          |         |
     * |         |          |         |
     * |_________|          |_________|
     *
     * @param src                     original bitmap of any size
     * @param w                       desired width in px
     * @param h                       desired height in px
     * @param horizontalCenterPercent determines which part of the src to crop from. Range from 0
     * .0f to 1.0f. The value determines which part of the src
     * maps to the horizontal center of the resulting bitmap.
     * @param verticalCenterPercent   determines which part of the src to crop from. Range from 0
     * .0f to 1.0f. The value determines which part of the src maps
     * to the vertical center of the resulting bitmap.
     * @return a copy of src conforming to the given width and height, or src itself if it already
     * matches the given width and height
     */
    fun crop(src: Bitmap?, w: Int, h: Int,
             horizontalCenterPercent: Float, verticalCenterPercent: Float): Bitmap? {
        require(!(horizontalCenterPercent < 0 || horizontalCenterPercent > 1 || verticalCenterPercent < 0 || verticalCenterPercent > 1)) {
            ("horizontalCenterPercent and verticalCenterPercent must be between 0.0f and "
                    + "1.0f, inclusive.")
        }
        val srcWidth = src!!.width
        val srcHeight = src.height
        // exit early if no resize/crop needed
        if (w == srcWidth && h == srcHeight) {
            return src
        }
        val m = Matrix()
        val scale = Math.max(
                w.toFloat() / srcWidth,
                h.toFloat() / srcHeight)
        m.setScale(scale, scale)
        val srcCroppedW: Int
        val srcCroppedH: Int
        var srcX: Int
        var srcY: Int
        srcCroppedW = Math.round(w / scale)
        srcCroppedH = Math.round(h / scale)
        srcX = (srcWidth * horizontalCenterPercent - srcCroppedW / 2).toInt()
        srcY = (srcHeight * verticalCenterPercent - srcCroppedH / 2).toInt()
        // Nudge srcX and srcY to be within the bounds of src
        srcX = Math.max(Math.min(srcX, srcWidth - srcCroppedW), 0)
        srcY = Math.max(Math.min(srcY, srcHeight - srcCroppedH), 0)
        return Bitmap.createBitmap(src, srcX, srcY, srcCroppedW, srcCroppedH, m,
                true /* filter */)
    }
}