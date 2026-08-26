package org.sparcs.soap.app.features.post.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
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

    var isRunning = false
    val haptic = LocalHapticFeedback.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .glassBorder(RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                painter = painterResource(upvoteImage),
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
                val formattedCount = targetCount.toString().replace('-', '\u2212')
                Text(
                    text = formattedCount,
                    color = if (myVote != null) tintColor else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        VerticalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .height(16.dp)
        )
        Icon(
            painter = painterResource(downvoteImage),
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