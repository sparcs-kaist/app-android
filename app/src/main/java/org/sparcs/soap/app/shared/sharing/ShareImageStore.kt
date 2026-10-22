package org.sparcs.soap.app.shared.sharing

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

class ShareImageStore @Inject constructor(@param:ApplicationContext private val context: Context) {
    suspend fun save(bitmap: Bitmap): Uri = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "shared_images")
        if (!directory.isDirectory && !directory.mkdirs()) throw IOException("Cannot create share directory")
        val cutoff = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        directory.listFiles()?.filter { it.isFile && it.lastModified() < cutoff }?.forEach { it.delete() }
        val file = File(directory, "${UUID.randomUUID()}.png")
        try {
            file.outputStream().use { bitmap.writePng(it) }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            file.delete()
            throw e
        }
    }

    suspend fun saveToGallery(bitmap: Bitmap): Uri = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Buddy_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Buddy")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: throw IOException("Cannot create gallery image")
        try {
            resolver.openOutputStream(uri)?.use { bitmap.writePng(it) } ?: throw IOException("Cannot open gallery image")
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
    }

    private fun Bitmap.writePng(stream: OutputStream) {
        if (!compress(Bitmap.CompressFormat.PNG, 100, stream)) throw IOException("Cannot encode share image")
    }
}
