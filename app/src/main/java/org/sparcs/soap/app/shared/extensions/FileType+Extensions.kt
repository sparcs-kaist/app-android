package org.sparcs.soap.app.shared.extensions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.TextSnippet
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.ui.graphics.vector.ImageVector

fun fileTypeIcon(mimeType: String, fileName: String): ImageVector {
    val mime = mimeType.lowercase()
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return when {
        mime.startsWith("image/") -> Icons.Outlined.Image
        mime == "application/pdf" || ext == "pdf" -> Icons.Outlined.PictureAsPdf
        mime.contains("spreadsheet") || mime.contains("excel") || ext in setOf("xls", "xlsx", "csv", "numbers") -> Icons.Outlined.TableChart
        mime.contains("presentation") || mime.contains("powerpoint") || ext in setOf("ppt", "pptx", "key") -> Icons.Outlined.Slideshow
        mime.contains("zip") || mime.contains("tar") || mime.contains("gzip") || mime.contains("compressed") || ext in setOf("zip", "tar", "gz", "7z", "rar") -> Icons.Outlined.FolderZip
        mime.startsWith("audio/") -> Icons.Outlined.AudioFile
        mime.startsWith("video/") -> Icons.Outlined.VideoFile
        mime.startsWith("text/") || ext in setOf("txt", "md") -> Icons.Outlined.TextSnippet
        else -> Icons.Outlined.Description
    }
}
