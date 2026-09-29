package org.sparcs.soap.app.features.friends.addFriends.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BluetoothDisabled
import androidx.compose.material.icons.automirrored.rounded.BluetoothSearching
import androidx.compose.material.icons.rounded.MobileOff
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.nearby.NearbyPeer
import org.sparcs.soap.app.domain.models.nearby.NearbyUnavailableReason
import org.sparcs.soap.app.features.friends.addFriends.NearbyFriendsViewState
import org.sparcs.soap.app.theme.ui.Theme

/**
 * The upper part of Add Friends: a fallback when discovery can't run, a radar
 * while nobody has been found, and the grid of people once someone has.
 */
@Composable
fun NearbyFriendsSection(
    state: NearbyFriendsViewState,
    onGrantPermission: () -> Unit,
    onOpenSettings: (NearbyUnavailableReason) -> Unit,
    onTapPeer: (NearbyPeer) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Keyed on which layout is showing, so peer updates inside the grid don't
    // cross-fade the whole section.
    val contentKey = when (state) {
        is NearbyFriendsViewState.Unavailable -> state.reason
        is NearbyFriendsViewState.Scanning -> state.peers.isEmpty()
    }
    AnimatedContent(
        targetState = contentKey,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
        label = "NearbyFriendsSection"
    ) { key ->
        when {
            key is NearbyUnavailableReason -> NearbyUnavailableView(
                reason = key,
                onGrantPermission = onGrantPermission,
                onOpenSettings = { onOpenSettings(key) }
            )

            key == true -> NearbySearchingView()
            // `state` is always the latest; the grid only reads it while shown.
            else -> NearbyPeersGrid(
                peers = (state as? NearbyFriendsViewState.Scanning)?.peers.orEmpty(),
                onTap = onTapPeer
            )
        }
    }
}

/** Section header; the trailing icon pulses while discovery runs. */
@Composable
fun NearbyHeader(isScanning: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.nearby_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.weight(1f))
        val searching = stringResource(R.string.nearby_searching)
        AnimatedVisibility(visible = isScanning, enter = fadeIn(), exit = fadeOut()) {
            val pulse by rememberInfiniteTransition(label = "NearbyHeaderPulse").animateFloat(
                initialValue = 1f,
                targetValue = 0.35f,
                animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                label = "NearbyHeaderAlpha"
            )
            Icon(
                Icons.AutoMirrored.Rounded.BluetoothSearching,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(20.dp)
                    .alpha(pulse)
                    .semantics { contentDescription = searching }
            )
        }
    }
}

// MARK: - Grid

@Composable
fun NearbyPeersGrid(
    peers: List<NearbyPeer>,
    onTap: (NearbyPeer) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 96.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(peers, key = { it.id }) { peer ->
            NearbyPeerItem(
                peer = peer,
                onClick = { onTap(peer) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

// MARK: - Searching

/** Rings ripple out from the centre, like Quick Share looking for devices. */
@Composable
fun NearbySearchingView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
            RippleRings(Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.BluetoothSearching,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        NearbyMessage(
            title = stringResource(R.string.nearby_searching_title),
            message = stringResource(R.string.nearby_searching_message)
        )
    }
}

@Composable
private fun RippleRings(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    val progress by rememberInfiniteTransition(label = "NearbyRipple").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2_400, easing = LinearEasing)),
        label = "NearbyRippleProgress"
    )
    Canvas(modifier) {
        val minRadius = 32.dp.toPx()
        val maxRadius = size.minDimension / 2
        val rings = 3
        repeat(rings) { index ->
            val phase = (progress + index.toFloat() / rings) % 1f
            drawCircle(
                color = color.copy(alpha = 0.45f * (1f - phase)),
                radius = minRadius + (maxRadius - minRadius) * phase,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

// MARK: - Unavailable

@Composable
fun NearbyUnavailableView(
    reason: NearbyUnavailableReason,
    onGrantPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                reason.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(36.dp)
            )
        }

        NearbyMessage(title = stringResource(reason.title), message = stringResource(reason.message))

        when (reason) {
            NearbyUnavailableReason.PermissionRequired -> Button(onClick = onGrantPermission) {
                Text(stringResource(R.string.nearby_permission_required_action))
            }

            NearbyUnavailableReason.PermissionDenied -> FilledTonalButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.nearby_open_settings))
            }

            NearbyUnavailableReason.BluetoothOff -> FilledTonalButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.nearby_bluetooth_settings))
            }

            NearbyUnavailableReason.Unsupported -> Unit
        }
    }
}

@Composable
private fun NearbyMessage(title: String, message: String) {
    Column(
        modifier = Modifier.widthIn(max = 360.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private val NearbyUnavailableReason.icon: ImageVector
    get() = when (this) {
        NearbyUnavailableReason.PermissionRequired -> Icons.AutoMirrored.Rounded.BluetoothSearching
        NearbyUnavailableReason.PermissionDenied -> Icons.Rounded.PanTool
        NearbyUnavailableReason.BluetoothOff -> Icons.Rounded.BluetoothDisabled
        NearbyUnavailableReason.Unsupported -> Icons.Rounded.MobileOff
    }

private val NearbyUnavailableReason.title: Int
    get() = when (this) {
        NearbyUnavailableReason.PermissionRequired -> R.string.nearby_permission_required_title
        NearbyUnavailableReason.PermissionDenied -> R.string.nearby_permission_denied_title
        NearbyUnavailableReason.BluetoothOff -> R.string.nearby_bluetooth_off_title
        NearbyUnavailableReason.Unsupported -> R.string.nearby_unsupported_title
    }

private val NearbyUnavailableReason.message: Int
    get() = when (this) {
        NearbyUnavailableReason.PermissionRequired -> R.string.nearby_permission_required_message
        NearbyUnavailableReason.PermissionDenied -> R.string.nearby_permission_denied_message
        NearbyUnavailableReason.BluetoothOff -> R.string.nearby_bluetooth_off_message
        NearbyUnavailableReason.Unsupported -> R.string.nearby_unsupported_message
    }

@Preview(showBackground = true)
@Composable
private fun NearbySearchingPreview() {
    Theme { NearbySearchingView() }
}
