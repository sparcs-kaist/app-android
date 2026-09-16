package org.sparcs.soap.app.domain.helpers

import android.content.ContentResolver
import android.graphics.ColorSpace
import android.graphics.ImageDecoder
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

object TimetablePhotoDecoder {
    suspend fun generate(resolver: ContentResolver, uri: Uri): TimetablePhotoPalette.Palette = withContext(Dispatchers.IO) {
        currentCoroutineContext().ensureActive()
        val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
            val scale = minOf(1.0, 96.0 / max(info.size.width, info.size.height))
            decoder.setTargetSize(max(1, (info.size.width * scale).roundToInt()), max(1, (info.size.height * scale).roundToInt()))
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
        }
        val pixels = try {
            IntArray(bitmap.width * bitmap.height).also {
                bitmap.getPixels(it, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            }
        } finally {
            bitmap.recycle()
        }
        withContext(Dispatchers.Default) { TimetablePhotoPalette.generate(pixels) }
    }
}
