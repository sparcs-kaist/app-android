package org.sparcs.soap.app.shared.extensions

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.sparcs.soap.app.domain.helpers.UploadImageDecoder

private const val PROFILE_IMAGE_MAX_DIMENSION = 1024
private const val PROFILE_IMAGE_MAX_MB = 1.0

suspend fun Uri.toProfileImagePart(context: Context): MultipartBody.Part? {
    val bitmap = UploadImageDecoder.decode(context.contentResolver, this, PROFILE_IMAGE_MAX_DIMENSION) ?: return null
    val bytes = withContext(Dispatchers.Default) {
        try {
            bitmap.compressForUpload(PROFILE_IMAGE_MAX_MB, PROFILE_IMAGE_MAX_DIMENSION)
        } finally {
            bitmap.recycle()
        }
    }
    return MultipartBody.Part.createFormData(
        "file", "profile_image.jpg", bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
    )
}
