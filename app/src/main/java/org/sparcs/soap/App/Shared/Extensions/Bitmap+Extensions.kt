package org.sparcs.soap.App.Shared.Extensions

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

fun Bitmap.compressForUpload(
    maxSizeMB: Double = 1.0,
    maxDimension: Int = 500
): ByteArray? {
    val ratio = minOf(maxDimension.toFloat() / width, maxDimension.toFloat() / height, 1f)
    val newWidth = (width * ratio).toInt()
    val newHeight = (height * ratio).toInt()
    val scaledBitmap = Bitmap.createScaledBitmap(this, newWidth, newHeight, true)

    val stream = ByteArrayOutputStream()
    var quality = 100
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

    while (stream.size() > maxSizeMB * 1024 * 1024 && quality > 10) {
        stream.reset()
        quality -= 5
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
    }

    return stream.toByteArray()
}

fun Bitmap.toByteArray(format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): ByteArray {
    val stream = ByteArrayOutputStream()
    this.compress(format, quality, stream)
    return stream.toByteArray()
}