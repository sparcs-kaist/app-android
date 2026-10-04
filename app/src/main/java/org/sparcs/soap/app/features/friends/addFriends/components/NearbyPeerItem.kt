package org.sparcs.soap.app.features.friends.addFriends.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.nearby.NearbyPeer
import org.sparcs.soap.app.domain.models.nearby.NearbyPeerState
import org.sparcs.soap.app.features.friends.components.InitialsAvatar
import org.sparcs.soap.app.features.friends.inClassColor
import org.sparcs.soap.app.shared.mocks.nearby.mockList
import org.sparcs.soap.app.theme.ui.Theme

private val AvatarSize = 72.dp

/**
 * One person in the nearby grid: their letter avatar, their name, and a
 * status line that changes as the exchange progresses.
 */
@Composable
fun NearbyPeerItem(
    peer: NearbyPeer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(peer.state) {
        if (peer.state == NearbyPeerState.Added) haptics.performHapticFeedback(HapticFeedbackType.Confirm)
    }

    val actionLabel = peer.state.actionLabel()?.let { stringResource(it) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                enabled = actionLabel != null,
                onClickLabel = actionLabel,
                role = Role.Button,
                onClick = onClick
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PeerAvatar(peer)
        Text(
            text = peer.name,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        PeerStatusLabel(peer.state)
    }
}

@Composable
private fun PeerAvatar(peer: NearbyPeer) {
    // Only pulses while they're waiting on us, so idle items don't redraw every frame.
    val pulse = if (peer.state == NearbyPeerState.Incoming) {
        val scale by rememberInfiniteTransition(label = "PeerPulse").animateFloat(
            initialValue = 1f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
            label = "PeerPulseScale"
        )
        scale
    } else {
        1f
    }
    val ringColor = when (peer.state) {
        NearbyPeerState.Incoming -> MaterialTheme.colorScheme.primary
        NearbyPeerState.Failed -> MaterialTheme.colorScheme.error
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .scale(pulse)
            .border(3.dp, ringColor, CircleShape)
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        InitialsAvatar(
            name = peer.name,
            size = AvatarSize,
            showsInitials = peer.state.overlay == PeerOverlay.None
        )
        AnimatedContent(
            targetState = peer.state.overlay,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "PeerOverlay"
        ) { overlay ->
            Box(
                modifier = Modifier
                    .size(AvatarSize)
                    .clip(CircleShape)
                    .background(if (overlay == PeerOverlay.None) Color.Transparent else Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                when (overlay) {
                    PeerOverlay.Progress -> CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(28.dp)
                    )

                    PeerOverlay.Done -> Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )

                    PeerOverlay.None -> Unit
                }
            }
        }
    }
}

@Composable
private fun PeerStatusLabel(state: NearbyPeerState) {
    val color = when (state) {
        NearbyPeerState.Incoming -> MaterialTheme.colorScheme.primary
        NearbyPeerState.Added -> inClassColor()
        NearbyPeerState.Failed -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    AnimatedContent(
        targetState = state,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "PeerStatus"
    ) { current ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (current == NearbyPeerState.Added) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            }
            Text(
                text = stringResource(current.statusLabel()),
                style = MaterialTheme.typography.labelMedium,
                color = color,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
        }
    }
}

private enum class PeerOverlay { None, Progress, Done }

/** Spinner while waiting, checkmark once added; nothing otherwise. */
private val NearbyPeerState.overlay: PeerOverlay
    get() = when (this) {
        NearbyPeerState.Requested, NearbyPeerState.Adding -> PeerOverlay.Progress
        NearbyPeerState.Added -> PeerOverlay.Done
        NearbyPeerState.Idle, NearbyPeerState.Declined, NearbyPeerState.Incoming, NearbyPeerState.Failed -> PeerOverlay.None
    }

private fun NearbyPeerState.statusLabel(): Int = when (this) {
    NearbyPeerState.Idle -> R.string.nearby_status_idle
    NearbyPeerState.Requested -> R.string.nearby_status_requested
    NearbyPeerState.Declined -> R.string.nearby_status_declined
    NearbyPeerState.Incoming -> R.string.nearby_status_incoming
    NearbyPeerState.Adding -> R.string.nearby_status_adding
    NearbyPeerState.Added -> R.string.nearby_status_added
    NearbyPeerState.Failed -> R.string.nearby_status_failed
}

/** What a tap does, for TalkBack; `null` when the item isn't actionable. */
private fun NearbyPeerState.actionLabel(): Int? = when (this) {
    NearbyPeerState.Idle, NearbyPeerState.Declined -> R.string.nearby_action_request
    NearbyPeerState.Requested -> R.string.nearby_action_cancel
    NearbyPeerState.Incoming -> R.string.nearby_action_accept
    NearbyPeerState.Failed -> R.string.nearby_action_retry
    NearbyPeerState.Adding, NearbyPeerState.Added -> null
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun NearbyPeerItemPreview() {
    Theme {
        val peers = NearbyPeer.mockList()
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            NearbyPeerState.entries.chunked(3).forEach { states ->
                Row(Modifier.fillMaxWidth()) {
                    states.forEachIndexed { index, state ->
                        NearbyPeerItem(
                            peer = peers[index].copy(state = state),
                            onClick = {},
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
