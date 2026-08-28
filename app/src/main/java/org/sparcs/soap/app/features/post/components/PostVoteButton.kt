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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sparcs.soap.R
import org.sparcs.soap.app.shared.extensions.adaptiveIconSize
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.downvote
import org.sparcs.soap.app.theme.ui.upvote

@Composable
fun PostVoteButton(
    myVote: Boolean?,
    votes: Int,
    onDownVote: () -> Unit,
    onUpVote: () -> Unit,
    enabled: Boolean,
    isCompact: Boolean = false,
) {
    val upVoteColor = upvote
    val downVoteColor = downvote
    val upvoteImage =
        if (myVote == true) R.drawable.baseline_arrow_up_bold else R.drawable.outline_arrow_up
    val downvoteImage =
        if (myVote == false) R.drawable.baseline_arrow_down_bold else R.drawable.outline_arrow_down

    val tintColor = when (myVote) {
        true -> upVoteColor
        false -> downVoteColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    val shapeRadius = if (isCompact) 14.dp else 16.dp
    val horizontalPadding = if (isCompact) 8.dp else 10.dp
    val verticalPadding = if (isCompact) 6.dp else 8.dp
    val baseStyle = MaterialTheme.typography.bodyMedium
    val textStyle = if (isCompact) baseStyle.copy(fontSize = 13.sp) else baseStyle

    var isRunning = false
    val haptic = LocalHapticFeedback.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .glassBorder(RoundedCornerShape(shapeRadius))
            .clip(RoundedCornerShape(shapeRadius))
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 6.dp)
        ) {
            Icon(
                painter = painterResource(upvoteImage),
                contentDescription = "UpVote",
                tint = if (myVote == true) upVoteColor else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .adaptiveIconSize(textStyle)
                    .clickable {
                        if (enabled && !isRunning) {
                            try {
                                isRunning = true
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                onUpVote()
                            } finally {
                                isRunning = false
                            }
                        }
                    }
            )
            AnimatedContent(
                targetState = votes,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically { -it } + fadeIn())
                            .togetherWith(slideOutVertically { it } + fadeOut())
                    } else {
                        (slideInVertically { it } + fadeIn())
                            .togetherWith(slideOutVertically { -it } + fadeOut())
                    }.using(SizeTransform(clip = false))
                },
                label = "VotesTransition"
            ) { targetCount ->
                val formattedCount = targetCount.toString().replace('-', '\u2212')
                Text(
                    text = formattedCount,
                    color = if (myVote != null) tintColor else MaterialTheme.colorScheme.onSurface,
                    style = textStyle,
                )
            }
        }

        VerticalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier
                .padding(horizontal = if (isCompact) 6.dp else 8.dp)
                .height(if (isCompact) 14.dp else 16.dp)
        )
        Icon(
            painter = painterResource(downvoteImage),
            contentDescription = "DownVote",
            tint = if (myVote == false) downVoteColor else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .adaptiveIconSize(textStyle)
                .clickable {
                    if (enabled && !isRunning) {
                        try {
                            isRunning = true
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onDownVote()
                        } finally {
                            isRunning = false
                        }
                    }
                }
        )
    }
}

@Composable
@Preview
private fun Preview() {
    Theme { PostVoteButton(true, 2, {}, {}, true) }
}