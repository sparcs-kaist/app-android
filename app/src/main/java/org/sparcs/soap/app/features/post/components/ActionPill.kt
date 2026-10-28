package org.sparcs.soap.app.features.post.components

import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.theme.ui.Theme

// Shared look for the post action pills: vote, comment, bookmark and share.

internal fun pillHeight(isCompact: Boolean) = if (isCompact) 32.dp else 36.dp

/** Material's chip and button pairing: a label style with a fixed-size icon beside it. */
@Composable
internal fun pillTextStyle(isCompact: Boolean): TextStyle =
    if (isCompact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge

internal fun pillIconSize(isCompact: Boolean) = if (isCompact) 16.dp else 18.dp

/**
 * Press feedback for the pills: Material's ripple, bounded to the part that was pressed, so it
 * shows even for a quick tap in a scrolling list. On a vote-colored pill it's black so it darkens
 * rather than washes out the color.
 */
@Composable
internal fun pillRipple(onColor: Boolean = false): Indication =
    ripple(color = if (onColor) Color.Black else Color.Unspecified)

/** Circular gray pill holding a single icon, sized to sit beside the vote and comment pills. */
@Composable
internal fun ActionPillIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    isCompact: Boolean = false,
    interaction: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(pillHeight(isCompact))
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(
                interactionSource = interaction,
                indication = pillRipple(),
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(pillIconSize(isCompact))
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            ActionPillIconButton(Icons.Outlined.BookmarkBorder, "", {})
            ActionPillIconButton(Icons.Filled.Bookmark, "", {})
            ActionPillIconButton(Icons.Outlined.Share, "", {})
        }
    }
}
