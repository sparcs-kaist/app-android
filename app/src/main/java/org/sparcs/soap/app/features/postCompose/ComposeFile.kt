package org.sparcs.soap.app.features.postCompose

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import org.sparcs.soap.app.domain.models.ara.AraPostAttachment
import org.sparcs.soap.app.features.post.components.displayName
import java.util.UUID

data class ComposeFile(
    val name: String,
    val size: Long,
    val mimeType: String,
    val source: Source,
    val id: String = UUID.randomUUID().toString(),
) {
    sealed interface Source {
        data class Picked(val uri: Uri) : Source
        data class Existing(val attachmentId: Int) : Source
    }

    companion object {
        val ALLOWED_MIME_TYPES = arrayOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/zip",
            "application/x-tar",
            "application/gzip",
            "video/mp4",
            "audio/mpeg",
        )

        fun existing(attachment: AraPostAttachment) = ComposeFile(
            name = attachment.displayName,
            size = attachment.size.toLong(),
            mimeType = attachment.mimeType,
            source = Source.Existing(attachment.id),
        )

        fun picked(resolver: ContentResolver, uri: Uri): ComposeFile {
            var name = uri.lastPathSegment ?: "attachment"
            var size = 0L
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)?.takeIf { it.isNotBlank() }?.let { name = it }
                    if (!cursor.isNull(1)) size = cursor.getLong(1)
                }
            }
            return ComposeFile(
                name = name,
                size = size,
                mimeType = resolver.getType(uri) ?: "application/octet-stream",
                source = Source.Picked(uri),
            )
        }
    }
}
