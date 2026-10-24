package org.sparcs.soap.app.features.post.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.unit.sp
import org.sparcs.soap.app.shared.extensions.adaptiveIconSize
import org.sparcs.soap.app.theme.ui.Theme

// Shared look for the post action pills: vote, comment, bookmark and share.

internal fun pillHeight(isCompact: Boolean) = if (isCompact) 32.dp else 36.dp

@Composable
internal fun pillTextStyle(isCompact: Boolean): TextStyle {
    val base = MaterialTheme.typography.bodyMedium
    return if (isCompact) base.copy(fontSize = 13.sp) else base
}

/**
 * Pressed state layer: darkens the pressed part of a pill. On a vote-colored pill the layer is
 * black so it darkens rather than washes out the color.
 */
@Composable
internal fun Modifier.pressedStateLayer(pressed: Boolean, onColor: Boolean = false): Modifier {
    val layer by animateColorAsState(
        when {
            !pressed -> Color.Transparent
            onColor -> Color.Black.copy(alpha = 0.12f)
            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        },
        label = "PressedStateLayer"
    )
    return background(layer)
}

/** Circular gray pill holding a single icon, sized to sit beside the vote and comment pills. */
@Composable
internal fun ActionPillIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    isCompact: Boolean = false,
    interaction: MutableInteractionSource = remember { MutableInteractionSource() },
    pressed: Boolean = interaction.collectIsPressedAsState().value,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(pillHeight(isCompact))
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .pressedStateLayer(pressed)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.adaptiveIconSize(pillTextStyle(isCompact))
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
            ActionPillIconButton(Icons.Outlined.Share, "", {}, pressed = true)
        }
    }
}
