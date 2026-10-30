package org.sparcs.soap.app.features.taxiChat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.navigationBar.components.BackButton
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.shared.tips.TipAction
import org.sparcs.soap.app.shared.tips.TipView
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun TaxiChatViewNavigationBar(
    room: TaxiRoom,
    riders: List<TaxiParticipant>,
    isCompactHeight: Boolean,
    onDismiss: () -> Unit,
    onOpenRideInfo: () -> Unit,
) {
    Column(modifier = Modifier.statusBarsPadding()) {
        Box(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Box(Modifier.align(Alignment.CenterStart)) {
                BackButton(onClick = onDismiss)
            }

            TaxiRideTitle(
                room = room,
                riders = riders,
                showsCapsule = !isCompactHeight,
                onClick = onOpenRideInfo,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 56.dp)
            )
        }

        TipView(
            BuddyTip.RideInfo,
            enabled = !isCompactHeight,
            actions = listOf(TipAction(stringResource(R.string.taxi_open_ride_info), onOpenRideInfo)),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )

        HorizontalDivider()
    }
}

@Composable
fun TaxiRideTitle(
    room: TaxiRoom,
    riders: List<TaxiParticipant>,
    showsCapsule: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val route = stringResource(R.string.taxi_route_summary, room.source.title.localized(), room.destination.title.localized())
    val openLabel = stringResource(R.string.taxi_open_ride_info)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "${room.title}, $route"
            role = Role.Button
            onClick(openLabel) { onClick(); true }
        }
    ) {
        TaxiRiderCluster(
            riders = riders,
            size = if (showsCapsule) 38.dp else 30.dp,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onClick)
        )

        if (showsCapsule) {
            Surface(
                onClick = onClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "${room.title} ${room.emojiIdentifier.display}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = route,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        Box(Modifier.fillMaxSize()) {
            val room = TaxiRoom.mock()
            TaxiChatViewNavigationBar(
                room = room,
                riders = room.participants,
                isCompactHeight = false,
                onDismiss = {},
                onOpenRideInfo = {}
            )
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
private fun CompactHeightPreview() {
    Theme {
        val room = TaxiRoom.mock()
        TaxiChatViewNavigationBar(
            room = room,
            riders = room.participants,
            isCompactHeight = true,
            onDismiss = {},
            onOpenRideInfo = {}
        )
    }
}
