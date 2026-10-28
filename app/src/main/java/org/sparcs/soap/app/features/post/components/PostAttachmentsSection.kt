package org.sparcs.soap.app.features.post.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraPostAttachment
import org.sparcs.soap.app.theme.ui.Theme
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

/**
 * Files attached to a post. Tapping one saves it to Downloads with progress shown on the card,
 * then offers to open it; a file that is already saved opens straight away. Messages appear in
 * [snackbarHostState] when given.
 */
@Composable
fun PostAttachmentsSection(
    attachments: List<AraPostAttachment>,
    snackbarHostState: SnackbarHostState? = null,
) {
    val fileAttachments = attachments.filter { !it.mimeType.startsWith("image/") }
    if (fileAttachments.isEmpty()) return

    val context = LocalContext.current
    val downloader = remember(context) { AttachmentDownloader(context) }
    val scope = rememberCoroutineScope()
    val savedMessage = stringResource(R.string.attachment_saved)
    val openLabel = stringResource(R.string.attachment_open)
    val noAppMessage = stringResource(R.string.attachment_no_app)

    // Show earlier downloads as saved, and keep tracking any still in progress.
    LaunchedEffect(fileAttachments) {
        fileAttachments.forEach { attachment -> launch { downloader.restore(attachment) } }
    }

    fun open(attachment: AraPostAttachment, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, attachment.mimeType)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            scope.launch { snackbarHostState?.showSnackbar(noAppMessage) }
        }
    }

    fun onAttachmentClick(attachment: AraPostAttachment) {
        when (val status = downloader.status(attachment)) {
            is AttachmentStatus.Downloading -> Unit
            is AttachmentStatus.Saved -> open(attachment, status.uri)
            AttachmentStatus.Idle -> scope.launch {
                val uri = downloader.download(attachment)
                if (uri == null) {
                    snackbarHostState?.showSnackbar(
                        context.getString(R.string.attachment_download_failed, attachment.filename)
                    )
                    return@launch
                }
                val result = snackbarHostState?.showSnackbar(
                    message = savedMessage,
                    actionLabel = openLabel,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) open(attachment, uri)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.attachments),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )

        fileAttachments.forEach { attachment ->
            AttachmentItem(
                attachment = attachment,
                status = downloader.status(attachment),
                onClick = { onAttachmentClick(attachment) }
            )
        }
    }
}

/** One file as an outlined card; the trailing icon shows whether it downloads, is saving, or opens. */
@Composable
private fun AttachmentItem(
    attachment: AraPostAttachment,
    status: AttachmentStatus,
    onClick: () -> Unit,
) {
    OutlinedCard(
        onClick = onClick,
        enabled = status !is AttachmentStatus.Downloading,
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            leadingContent = { Icon(Icons.Outlined.Description, contentDescription = null) },
            headlineContent = {
                Text(attachment.filename, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = { Text(formatFileSize(attachment.size)) },
            trailingContent = {
                when (status) {
                    is AttachmentStatus.Downloading -> {
                        val label = stringResource(R.string.downloading)
                        val indicatorModifier = Modifier
                            .size(24.dp)
                            .semantics { contentDescription = label }
                        if (status.progress == null) {
                            CircularProgressIndicator(modifier = indicatorModifier, strokeWidth = 2.dp)
                        } else {
                            CircularProgressIndicator(
                                progress = { status.progress },
                                modifier = indicatorModifier,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    is AttachmentStatus.Saved -> Icon(
                        Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = stringResource(R.string.attachment_open)
                    )

                    AttachmentStatus.Idle -> Icon(
                        Icons.Outlined.Download,
                        contentDescription = stringResource(id = R.string.download)
                    )
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

private fun formatFileSize(sizeBytes: Int): String {
    if (sizeBytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(sizeBytes.toDouble()) / log10(1024.0)).toInt()
    return String.format(Locale.ROOT, "%.1f %s", sizeBytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
}

@Preview(showBackground = true)
@Composable
private fun PostAttachmentsSectionPreview() {
    Theme {
        PostAttachmentsSection(
            listOf(
                AraPostAttachment(1, Date(), null, "Lecture notes week 3.pdf", 2_457_600, "application/pdf"),
                AraPostAttachment(2, Date(), null, "Application form.hwp", 48_128, "application/x-hwp"),
            )
        )
    }
}
