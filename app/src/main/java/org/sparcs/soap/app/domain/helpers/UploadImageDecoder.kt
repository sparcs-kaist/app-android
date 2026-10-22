package org.sparcs.soap.app.domain.helpers

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.max
import kotlin.math.roundToInt

object UploadImageDecoder {
    const val MAX_DIMENSION = 2048

    suspend fun decode(resolver: ContentResolver, uri: Uri, maxDimension: Int = MAX_DIMENSION): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                    val longest = max(info.size.width, info.size.height)
                    if (longest > maxDimension) {
                        val scale = maxDimension.toDouble() / longest
                        decoder.setTargetSize(
                            max(1, (info.size.width * scale).roundToInt()),
                            max(1, (info.size.height * scale).roundToInt()),
                        )
                    }
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to decode image for upload")
                null
            }
        }
}
