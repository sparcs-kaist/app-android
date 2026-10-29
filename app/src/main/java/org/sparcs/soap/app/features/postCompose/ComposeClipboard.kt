package org.sparcs.soap.app.features.postCompose

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

internal object ComposeClipboard {
    private const val IMAGE_TYPES = "image/*"
    private const val CACHE_DIRECTORY = "compose-pasted"

    fun hasOnlyImages(context: Context): Boolean {
        val description = context.clipboard?.primaryClipDescription ?: return false
        return description.hasMimeType(IMAGE_TYPES) && !description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
    }

    fun hasImages(context: Context): Boolean =
        context.clipboard?.primaryClipDescription?.hasMimeType(IMAGE_TYPES) == true

    suspend fun pasteImages(context: Context): List<Uri> {
        val clip = context.clipboard?.primaryClip ?: return emptyList()
        val uris = (0 until clip.itemCount).mapNotNull { clip.getItemAt(it).uri }
        return withContext(Dispatchers.IO) {
            uris.filter { context.contentResolver.getType(it)?.startsWith("image/") == true }
                .mapNotNull { copyToCache(context, it) }
        }
    }

    private fun copyToCache(context: Context, uri: Uri): Uri? = runCatching {
        val directory = File(context.cacheDir, CACHE_DIRECTORY).apply { mkdirs() }
        val file = File(directory, UUID.randomUUID().toString())
        val copied = context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
        if (copied == null || copied == 0L) null else Uri.fromFile(file)
    }.getOrNull()

    private val Context.clipboard: ClipboardManager?
        get() = getSystemService(ClipboardManager::class.java)
}
