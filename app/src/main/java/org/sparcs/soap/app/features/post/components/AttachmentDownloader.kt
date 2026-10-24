package org.sparcs.soap.app.features.post.components

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.core.content.edit
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraPostAttachment

/** Where an attachment's download stands. */
internal sealed interface AttachmentStatus {
    data object Idle : AttachmentStatus

    /** [progress] is null until the server reports the file size. */
    data class Downloading(val progress: Float?) : AttachmentStatus

    data class Saved(val uri: Uri) : AttachmentStatus
}

/**
 * Saves attachments to Downloads through the system DownloadManager and tracks each one, so the
 * UI can show progress and open a file that is already saved instead of downloading it again.
 * Download IDs are kept in preferences, so this survives leaving the post or restarting the app.
 */
@Stable
internal class AttachmentDownloader(context: Context) {
    private val appContext = context.applicationContext
    private val manager = appContext.getSystemService(DownloadManager::class.java)
    private val preferences = appContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    private val statuses = mutableStateMapOf<Int, AttachmentStatus>()

    fun status(attachment: AraPostAttachment): AttachmentStatus =
        statuses[attachment.id] ?: AttachmentStatus.Idle

    /**
     * Picks up an earlier download: marks it saved if its file still exists, keeps tracking it if
     * it is still running, and forgets it otherwise.
     */
    suspend fun restore(attachment: AraPostAttachment) {
        val id = storedId(attachment) ?: return
        track(attachment, id)
    }

    /** Downloads [attachment] and returns the saved file, or null if the download failed. */
    suspend fun download(attachment: AraPostAttachment): Uri? {
        val url = attachment.file?.toString() ?: return null
        val id = try {
            manager.enqueue(
                DownloadManager.Request(url.toUri())
                    .setTitle(attachment.filename)
                    .setDescription(appContext.getString(R.string.downloading))
                    .setMimeType(attachment.mimeType)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        safeFileName(attachment.filename)
                    )
            )
        } catch (_: Exception) {
            return null
        }
        preferences.edit { putLong(key(attachment), id) }
        return track(attachment, id)
    }

    /** Drops a saved file the user has since deleted, so the next tap downloads it again. */
    fun forget(attachment: AraPostAttachment) {
        preferences.edit { remove(key(attachment)) }
        statuses.remove(attachment.id)
    }

    /** Follows download [id] until it finishes, updating the attachment's status as it goes. */
    private suspend fun track(attachment: AraPostAttachment, id: Long): Uri? {
        while (true) {
            val progress = query(id)
            when (progress?.status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    val uri = manager.getUriForDownloadedFile(id)
                    if (uri != null && exists(uri)) {
                        statuses[attachment.id] = AttachmentStatus.Saved(uri)
                        return uri
                    }
                    forget(attachment)
                    return null
                }

                DownloadManager.STATUS_PENDING,
                DownloadManager.STATUS_RUNNING,
                DownloadManager.STATUS_PAUSED -> {
                    statuses[attachment.id] = AttachmentStatus.Downloading(progress.fraction)
                    delay(POLL_INTERVAL_MS)
                }

                // Failed, or removed from the system's download list.
                else -> {
                    forget(attachment)
                    return null
                }
            }
        }
    }

    private class Progress(val status: Int, val fraction: Float?)

    private fun query(id: Long): Progress? =
        manager.query(DownloadManager.Query().setFilterById(id))?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            val done = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            Progress(status, if (total > 0) (done.toFloat() / total).coerceIn(0f, 1f) else null)
        }

    private fun exists(uri: Uri): Boolean = try {
        appContext.contentResolver.openFileDescriptor(uri, "r")?.use { true } ?: false
    } catch (_: Exception) {
        false
    }

    private fun storedId(attachment: AraPostAttachment): Long? =
        preferences.getLong(key(attachment), -1L).takeIf { it >= 0 }

    private fun key(attachment: AraPostAttachment) = "attachment_${attachment.id}"

    private companion object {
        const val PREFERENCES = "ara_attachment_downloads"
        const val POLL_INTERVAL_MS = 300L

        /** Replaces characters that are not allowed in a file name. */
        fun safeFileName(name: String) = name.replace(Regex("""[\\/:*?"<>|]"""), "_")
    }
}
