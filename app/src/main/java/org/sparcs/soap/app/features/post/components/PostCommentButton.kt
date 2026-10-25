package org.sparcs.soap.app.features.post.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.theme.ui.Theme

/** Gray pill with a comment icon and count, matching [PostVoteButton]. */
@Composable
fun PostCommentButton(
    commentCount: Int,
    isCompact: Boolean = false,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    CommentPill(
        commentCount = commentCount,
        isCompact = isCompact,
        pressed = pressed,
        interaction = interaction,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onClick()
        }
    )
}

@Composable
private fun CommentPill(
    commentCount: Int,
    isCompact: Boolean,
    pressed: Boolean,
    interaction: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit = {},
) {
    val textStyle = pillTextStyle(isCompact)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 5.dp else 6.dp),
        modifier = Modifier
            .height(pillHeight(isCompact))
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .pressedStateLayer(pressed)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = if (isCompact) 10.dp else 12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.ChatBubbleOutline,
            contentDescription = "Comments",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(pillIconSize(isCompact))
        )

        AnimatedContent(
            targetState = commentCount,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInVertically { -it } + fadeIn())
                        .togetherWith(slideOutVertically { it } + fadeOut())
                } else {
                    (slideInVertically { it } + fadeIn())
                        .togetherWith(slideOutVertically { -it } + fadeOut())
                }.using(SizeTransform(clip = false))
            },
            label = "CommentCountTransition"
        ) { targetCount ->
            Text(
                text = targetCount.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                style = textStyle,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            CommentPill(12, isCompact = false, pressed = false)
            CommentPill(12, isCompact = false, pressed = true)
            CommentPill(12, isCompact = true, pressed = false)
        }
    }
}
