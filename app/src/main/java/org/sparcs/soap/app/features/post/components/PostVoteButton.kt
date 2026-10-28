package org.sparcs.soap.app.features.post.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.downvote
import org.sparcs.soap.app.theme.ui.upvote

/**
 * Pill with an upvote half and a downvote half around the score. Neutral, it is a gray pill with
 * outlined arrows; once voted, the whole pill takes the vote color with a filled arrow. Pressing
 * a half darkens it, and on a neutral pill also previews the vote color on that arrow.
 */
@Composable
fun PostVoteButton(
    myVote: Boolean?,
    votes: Int,
    onDownVote: () -> Unit,
    onUpVote: () -> Unit,
    enabled: Boolean,
    isCompact: Boolean = false,
) {
    val upInteraction = remember { MutableInteractionSource() }
    val downInteraction = remember { MutableInteractionSource() }
    val upPressed by upInteraction.collectIsPressedAsState()
    val downPressed by downInteraction.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    VotePill(
        myVote = myVote,
        votes = votes,
        enabled = enabled,
        isCompact = isCompact,
        upPressed = upPressed,
        downPressed = downPressed,
        upInteraction = upInteraction,
        downInteraction = downInteraction,
        onUpVote = {
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            onUpVote()
        },
        onDownVote = {
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            onDownVote()
        }
    )
}

@Composable
private fun VotePill(
    myVote: Boolean?,
    votes: Int,
    enabled: Boolean,
    isCompact: Boolean,
    upPressed: Boolean,
    downPressed: Boolean,
    upInteraction: MutableInteractionSource = remember { MutableInteractionSource() },
    downInteraction: MutableInteractionSource = remember { MutableInteractionSource() },
    onUpVote: () -> Unit = {},
    onDownVote: () -> Unit = {},
) {
    val container by animateColorAsState(
        when (myVote) {
            true -> upvote
            false -> downvote
            null -> MaterialTheme.colorScheme.surfaceContainerHighest
        },
        label = "VoteContainer"
    )
    val content = if (myVote == null) MaterialTheme.colorScheme.onSurface else Color.White
    val textStyle = pillTextStyle(isCompact)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(pillHeight(isCompact))
            .clip(CircleShape)
            .background(container)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 6.dp),
            modifier = Modifier
                .fillMaxHeight()
                .clickable(
                    interactionSource = upInteraction,
                    indication = pillRipple(onColor = myVote != null),
                    enabled = enabled,
                    role = Role.Button,
                    onClickLabel = "UpVote",
                    onClick = onUpVote
                )
                .padding(start = if (isCompact) 8.dp else 10.dp, end = if (isCompact) 6.dp else 8.dp)
        ) {
            VoteArrow(
                up = true,
                tint = if (myVote == null && upPressed) upvote else content,
                isCompact = isCompact
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
                Text(
                    text = targetCount.toString().replace('-', '−'),
                    color = content,
                    style = textStyle,
                )
            }
        }

        // Divider between the halves.
        Row(
            Modifier
                .width(1.dp)
                .height(if (isCompact) 14.dp else 16.dp)
                .background(content.copy(alpha = if (myVote == null) 0.2f else 0.35f))
        ) {}

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxHeight()
                .clickable(
                    interactionSource = downInteraction,
                    indication = pillRipple(onColor = myVote != null),
                    enabled = enabled,
                    role = Role.Button,
                    onClickLabel = "DownVote",
                    onClick = onDownVote
                )
                .padding(start = if (isCompact) 6.dp else 8.dp, end = if (isCompact) 8.dp else 10.dp)
        ) {
            VoteArrow(
                up = false,
                tint = if (myVote == null && downPressed) downvote else content,
                isCompact = isCompact
            )
        }
    }
}

/**
 * Material's up or down arrow. The arrow shape stays the same once voted, since the whole pill
 * then takes the vote colour.
 */
@Composable
internal fun VoteArrow(up: Boolean, tint: Color, isCompact: Boolean) {
    Icon(
        imageVector = if (up) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
        contentDescription = if (up) "UpVote" else "DownVote",
        tint = tint,
        modifier = Modifier.size(pillIconSize(isCompact))
    )
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            listOf<Boolean?>(null, true, false).forEach { vote ->
                VotePill(vote, 3, true, false, upPressed = false, downPressed = false)
                VotePill(vote, 3, true, false, upPressed = true, downPressed = false)
                VotePill(vote, 3, true, false, upPressed = false, downPressed = true)
            }
            VotePill(null, 3, true, true, upPressed = false, downPressed = false)
        }
    }
}
