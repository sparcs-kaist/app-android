package org.sparcs.soap.app.features.friends.addFriends.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.nearby.NearbyPeer
import org.sparcs.soap.app.features.friends.components.InitialsAvatar
import org.sparcs.soap.app.shared.mocks.nearby.mockList
import org.sparcs.soap.app.theme.ui.Theme

/**
 * Pending requests, answered one at a time: the oldest is shown with a count
 * of the rest, and answering it brings the next one in.
 */
@Composable
fun IncomingRequestQueue(
    peers: List<NearbyPeer>,
    onAccept: (NearbyPeer) -> Unit,
    onDecline: (NearbyPeer) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    var previousCount by remember { mutableIntStateOf(peers.size) }
    LaunchedEffect(peers.size) {
        if (peers.size > previousCount) haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
        previousCount = peers.size
    }

    AnimatedContent(
        targetState = peers.firstOrNull(),
        contentKey = { it?.id },
        transitionSpec = { (fadeIn() + slideInVertically { it / 3 }) togetherWith fadeOut() },
        modifier = modifier,
        label = "IncomingRequestQueue"
    ) { front ->
        if (front != null) {
            IncomingRequestCard(
                peer = front,
                remainingCount = peers.size - 1,
                onAccept = { onAccept(front) },
                onDecline = { onDecline(front) }
            )
        }
    }
}

/** Shown above the code controls when someone nearby asks to add you. */
@Composable
fun IncomingRequestCard(
    peer: NearbyPeer,
    remainingCount: Int,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InitialsAvatar(name = peer.name, size = 44.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        peer.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        stringResource(R.string.nearby_incoming_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (remainingCount > 0) {
                    val more = pluralStringResource(R.plurals.nearby_more_requests, remainingCount, remainingCount)
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.semantics { contentDescription = more }
                    ) {
                        Text(
                            "+$remainingCount",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDecline, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.nearby_decline))
                }
                Button(onClick = onAccept, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.nearby_accept))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IncomingRequestCardPreview() {
    Theme {
        IncomingRequestQueue(
            peers = NearbyPeer.mockList().take(4),
            onAccept = {},
            onDecline = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
