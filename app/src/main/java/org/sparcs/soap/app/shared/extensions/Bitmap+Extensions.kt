package org.sparcs.soap.app.shared.extensions

import android.graphics.Bitmap
import androidx.core.graphics.scale
import kotlin.math.max
import kotlin.math.roundToInt
import java.io.ByteArrayOutputStream

private const val MIN_QUALITY = 70
private const val MIN_DIMENSION = 640

fun Bitmap.compressForUpload(maxSizeMB: Double, maxDimension: Int): ByteArray {
    val maxBytes = (maxSizeMB * 1024 * 1024).toInt()
    var dimension = minOf(maxDimension, max(width, height))
    while (true) {
        val scaled = scaledTo(dimension)
        try {
            for (quality in intArrayOf(90, 80, MIN_QUALITY)) {
                val bytes = scaled.jpegBytes(quality)
                if (bytes.size <= maxBytes) return bytes
            }
            if (dimension <= MIN_DIMENSION) return scaled.jpegBytes(MIN_QUALITY)
        } finally {
            if (scaled !== this) scaled.recycle()
        }
        dimension = max(MIN_DIMENSION, (dimension * 0.75).roundToInt())
    }
}

private fun Bitmap.scaledTo(maxDimension: Int): Bitmap {
    val ratio = maxDimension.toFloat() / max(width, height)
    if (ratio >= 1f) return this
    return scale(max(1, (width * ratio).roundToInt()), max(1, (height * ratio).roundToInt()))
}

private fun Bitmap.jpegBytes(quality: Int): ByteArray =
    ByteArrayOutputStream().also { compress(Bitmap.CompressFormat.JPEG, quality, it) }.toByteArray()

fun Bitmap.toByteArray(format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): ByteArray {
    val stream = ByteArrayOutputStream()
    this.compress(format, quality, stream)
    return stream.toByteArray()
}
