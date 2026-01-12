package org.sparcs.App.Shared.Extensions

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

fun Uri.toMultipartBody(context: Context): MultipartBody.Part? {
    return try {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(this) ?: "image/jpeg"
        val inputStream = contentResolver.openInputStream(this)
        val bytes = inputStream?.use { it.readBytes() } ?: return null

        val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        MultipartBody.Part.createFormData("file", "profile_image.jpg", requestFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}