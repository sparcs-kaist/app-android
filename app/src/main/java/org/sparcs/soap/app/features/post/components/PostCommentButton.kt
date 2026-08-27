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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sparcs.soap.app.shared.extensions.adaptiveIconSize
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun PostCommentButton(
    commentCount: Int,
    isCompact: Boolean = false,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    val shapeRadius = if (isCompact) 14.dp else 16.dp
    val horizontalPadding = if (isCompact) 8.dp else 10.dp
    val verticalPadding = if (isCompact) 6.dp else 8.dp
    val baseStyle = MaterialTheme.typography.bodyMedium
    val textStyle = if (isCompact) baseStyle.copy(fontSize = 13.sp) else baseStyle

    val shape = RoundedCornerShape(shapeRadius)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .glassBorder(shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.background)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onClick()
            }
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Comment,
            contentDescription = "Comments",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.adaptiveIconSize(textStyle)
        )

        Spacer(Modifier.width(if (isCompact) 5.dp else 6.dp))

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
                style = textStyle,
            )
        }
    }
}

@Composable
@Preview
private fun Preview() {
    var comment by remember { mutableIntStateOf(1) }
    Theme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PostCommentButton(comment, isCompact = false, { comment += 1 })
            PostCommentButton(comment, isCompact = false, { comment += 1 })
        }
    }
}