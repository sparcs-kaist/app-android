package org.sparcs.soap.app.features.post.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

/** Bookmark pill; the icon fills in once the post is bookmarked. */
@Composable
fun PostBookmarkButton(
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    ActionPillIconButton(
        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
        contentDescription = stringResource(R.string.to_bookmark),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            onToggleBookmark()
        }
    )
}

@Composable
@Preview
private fun Preview() {
    Theme { PostBookmarkButton(true) {} }
}
