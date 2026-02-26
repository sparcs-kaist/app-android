package org.sparcs.soap.App.Shared.Extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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

fun Context.openUri(uri: Uri, packageName: String? = null) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        this.startActivity(intent)
    } catch (e: Exception) {
        if (packageName != null) {
            try {
                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                this.startActivity(marketIntent)
            } catch (e2: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                this.startActivity(webIntent)
            }
        } else {
            Toast.makeText(this, "앱을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}