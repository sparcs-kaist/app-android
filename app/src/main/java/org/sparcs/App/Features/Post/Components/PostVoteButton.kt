package org.sparcs.App.Features.Post.Components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.App.theme.ui.Theme
import org.sparcs.App.theme.ui.upvote
import org.sparcs.App.theme.ui.downvote

@Composable
fun PostVoteButton(
    myVote: Boolean?,
    votes: Int,
    onDownVote: () -> Unit,
    onUpVote: () -> Unit,
    enabled: Boolean,
) {
    val upVoteColor = upvote
    val downVoteColor = downvote

    val tintColor = when (myVote) {
        true -> upVoteColor
        false -> downVoteColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    var isRunning = false
    val haptic = LocalHapticFeedback.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.ArrowUpward,
                contentDescription = "UpVote",
                tint = if (myVote == true) upVoteColor else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(20.dp)
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
                label = "VotesTransition"
            ) { targetCount ->
                Text(
                    text = targetCount.toString(),
                    color = if (myVote != null) tintColor else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        VerticalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .height(20.dp)
        )
        Icon(
            imageVector = Icons.Outlined.ArrowDownward,
            contentDescription = "DownVote",
            tint = if (myVote == false) downVoteColor else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(20.dp)
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