package org.sparcs.soap.app.features.postList.components.postListRow

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.isHiddenForBlockedAuthor

val AraPost.isRead: Boolean
    get() = readStatus == AraPost.ReadStatus.READ

val AraPost.isUpdatedSinceRead: Boolean
    get() = readStatus == AraPost.ReadStatus.UPDATED

private const val READ_TITLE_ALPHA = 0.5f
private const val READ_META_ALPHA = 0.38f
private const val READ_ACCENT_ALPHA = 0.5f

@Composable
fun AraPost.titleColor(): Color = when {
    isHidden -> MaterialTheme.colorScheme.onSurfaceVariant
    isRead -> MaterialTheme.colorScheme.onSurface.copy(alpha = READ_TITLE_ALPHA)
    else -> MaterialTheme.colorScheme.onSurface
}

@Composable
fun AraPost.metaColor(): Color =
    if (isRead) MaterialTheme.colorScheme.onSurface.copy(alpha = READ_META_ALPHA)
    else MaterialTheme.colorScheme.onSurfaceVariant

@Composable
fun AraPost.accentColor(accent: Color = MaterialTheme.colorScheme.primary): Color =
    if (isRead) accent.copy(alpha = accent.alpha * READ_ACCENT_ALPHA) else accent

@get:StringRes
val AraPost.hiddenReasonRes: Int
    get() = when {
        isHiddenForBlockedAuthor -> R.string.post_hidden_blocked_author
        isNSFW -> R.string.this_post_contains_nsfw_content
        isPolitical -> R.string.this_post_contains_political_content
        else -> R.string.this_post_is_hidden
    }

val AraPost.hiddenIcon: ImageVector
    get() = when {
        isHiddenForBlockedAuthor -> Icons.Outlined.PersonOff
        isNSFW -> Icons.Outlined.ReportProblem
        isPolitical -> Icons.Outlined.Campaign
        else -> Icons.Outlined.VisibilityOff
    }
