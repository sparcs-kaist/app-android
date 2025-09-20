package com.example.soap.Shared.Extensions

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

fun Bitmap.toByteArray(format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): ByteArray {
    val stream = ByteArrayOutputStream()
    this.compress(format, quality, stream)
    return stream.toByteArray()
}