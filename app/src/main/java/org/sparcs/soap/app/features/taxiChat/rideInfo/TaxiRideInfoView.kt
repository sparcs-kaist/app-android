package org.sparcs.soap.app.features.taxiChat.rideInfo

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.EmojiPeople
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.taxiChat.components.TaxiRiderCluster
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiRiderAvatar
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.seatsOpenContainer
import org.sparcs.soap.app.theme.ui.onSeatsOpenContainer

@Composable
fun TaxiRideInfoView(
    room: TaxiRoom,
    riders: List<TaxiParticipant>,
    myUserID: String?,
    isArrived: Boolean,
    hasCarrier: Boolean,
    canMarkArrival: Boolean,
    canLeave: Boolean,
    onToggleArrival: () -> Unit,
    onToggleCarrier: () -> Unit,
    onShare: () -> Unit,
    onCallTaxi: () -> Unit,
    onReport: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showsLeaveConfirmation by remember { mutableStateOf(false) }
    val sortedRiders = remember(riders, myUserID) {
        riders.withIndex()
            .sortedWith(compareBy({ if (it.value.id == myUserID) 0 else 1 }, { it.index }))
            .map { it.value }
    }
    val arrived = sortedRiders.filter { it.isArrived }
    val onTheWay = sortedRiders.filterNot { it.isArrived }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        item(key = "header") { RideHeader(room, riders) }

        item(key = "actions") {
            RideActions(
                canReport = room.participants.size > 1,
                onShare = onShare,
                onCallTaxi = onCallTaxi,
                onReport = onReport
            )
        }

        item(key = "status") {
            MyStatus(isArrived, hasCarrier, canMarkArrival, onToggleArrival, onToggleCarrier)
        }

        riderSection(
            key = "arrived",
            title = { stringResource(R.string.taxi_arrived_section, arrived.size) },
            icon = Icons.Filled.CheckCircle,
            iconTint = { MaterialTheme.colorScheme.primary },
            riders = arrived,
            myUserID = myUserID
        )
        riderSection(
            key = "onTheWay",
            title = { stringResource(R.string.taxi_on_the_way_section, onTheWay.size) },
            icon = Icons.AutoMirrored.Outlined.DirectionsWalk,
            iconTint = { MaterialTheme.colorScheme.onSurfaceVariant },
            riders = onTheWay,
            myUserID = myUserID
        )

        item(key = "leave") {
            TextButton(
                onClick = { showsLeaveConfirmation = true },
                enabled = canLeave,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.leave))
            }
        }
    }

    if (showsLeaveConfirmation) {
        AlertDialog(
            onDismissRequest = { showsLeaveConfirmation = false },
            title = { Text(stringResource(R.string.taxi_leave_room_title)) },
            text = { Text(stringResource(R.string.taxi_leave_room_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showsLeaveConfirmation = false
                        onLeave()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.leave)) }
            },
            dismissButton = {
                TextButton(onClick = { showsLeaveConfirmation = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun RideHeader(room: TaxiRoom, riders: List<TaxiParticipant>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        TaxiRiderCluster(riders, size = 96.dp, modifier = Modifier.padding(bottom = 6.dp))
        Text(
            text = room.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.taxi_route_summary, room.source.title.localized(), room.destination.title.localized()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        TaxiDepartureChips(room.departAt, modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
private fun RideActions(canReport: Boolean, onShare: () -> Unit, onCallTaxi: () -> Unit, onReport: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        ActionTile(Icons.Outlined.Share, stringResource(R.string.share), onShare, Modifier.weight(1f))
        ActionTile(Icons.Outlined.LocalTaxi, stringResource(R.string.call_taxi), onCallTaxi, Modifier.weight(1f))
        ActionTile(Icons.Outlined.Report, stringResource(R.string.report), onReport, Modifier.weight(1f), enabled = canReport)
    }
}

@Composable
private fun ActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.38f),
        modifier = modifier.heightIn(min = 64.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Icon(icon, contentDescription = null)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun MyStatus(
    isArrived: Boolean,
    hasCarrier: Boolean,
    canMarkArrival: Boolean,
    onToggleArrival: () -> Unit,
    onToggleCarrier: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        SectionHeader(stringResource(R.string.taxi_my_status))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatusTile(
                isOn = isArrived,
                enabled = canMarkArrival,
                on = Icons.Filled.CheckCircle to stringResource(R.string.taxi_arrival_complete),
                off = Icons.Outlined.EmojiPeople to stringResource(R.string.taxi_im_here),
                onClick = onToggleArrival,
                modifier = Modifier.weight(1f)
            )
            StatusTile(
                isOn = hasCarrier,
                enabled = true,
                on = Icons.Filled.Luggage to stringResource(R.string.taxi_with_luggage),
                off = Icons.Outlined.Luggage to stringResource(R.string.taxi_no_luggage),
                onClick = onToggleCarrier,
                modifier = Modifier.weight(1f)
            )
        }
        if (!canMarkArrival) {
            Text(
                text = stringResource(R.string.taxi_arrival_only_before_departure),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusTile(
    isOn: Boolean,
    enabled: Boolean,
    on: Pair<ImageVector, String>,
    off: Pair<ImageVector, String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val container by animateColorAsState(if (isOn) scheme.seatsOpenContainer else scheme.surfaceContainerHigh, label = "tile")
    val content by animateColorAsState(if (isOn) scheme.onSeatsOpenContainer else scheme.onSurface, label = "tileContent")

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = content.copy(alpha = if (enabled) 1f else 0.38f),
        modifier = modifier
            .heightIn(min = 80.dp)
            .semantics {
                role = Role.Switch
                selected = isOn
            }
    ) {
        AnimatedContent(targetState = if (isOn) on else off, label = "statusTile") { (icon, label) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                modifier = Modifier.padding(vertical = 14.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
                Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun LazyListScope.riderSection(
    key: String,
    title: @Composable () -> String,
    icon: ImageVector,
    iconTint: @Composable () -> Color,
    riders: List<TaxiParticipant>,
    myUserID: String?,
) {
    if (riders.isEmpty()) return
    item(key = "$key-header") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = iconTint(), modifier = Modifier.size(18.dp))
            SectionHeader(title())
        }
    }
    items(riders, key = { "$key-${it.id}" }) { rider -> RiderRow(rider, isMe = rider.id == myUserID) }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun RiderRow(rider: TaxiParticipant, isMe: Boolean) {
    ListItem(
        leadingContent = { TaxiRiderAvatar(rider, size = 36.dp, showsLuggage = false) },
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(rider.nickname, fontWeight = FontWeight.SemiBold)
                if (rider.badge) {
                    Icon(
                        Icons.Filled.VerifiedUser,
                        contentDescription = stringResource(R.string.taxi_phone_verified),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (isMe) {
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                        Text(
                            text = stringResource(R.string.taxi_me),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        trailingContent = if (rider.hasCarrier) {
            {
                Icon(
                    Icons.Filled.Luggage,
                    contentDescription = stringResource(R.string.taxi_with_luggage),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else null,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
@Preview(showBackground = true, heightDp = 1000)
private fun Preview() {
    Theme {
        val room = TaxiRoom.mock()
        val riders = listOf(room.participants[0].copy(isArrived = true, hasCarrier = true), room.participants[1])
        TaxiRideInfoView(
            room = room,
            riders = riders,
            myUserID = riders[1].id,
            isArrived = false,
            hasCarrier = true,
            canMarkArrival = true,
            canLeave = true,
            onToggleArrival = {},
            onToggleCarrier = {},
            onShare = {},
            onCallTaxi = {},
            onReport = {},
            onLeave = {}
        )
    }
}

@Composable
@Preview(showBackground = true, heightDp = 800)
private fun DepartedPreview() {
    Theme {
        val room = TaxiRoom.mock().copy(isDeparted = true)
        TaxiRideInfoView(
            room = room,
            riders = room.participants.take(1),
            myUserID = room.participants[0].id,
            isArrived = true,
            hasCarrier = false,
            canMarkArrival = false,
            canLeave = false,
            onToggleArrival = {},
            onToggleCarrier = {},
            onShare = {},
            onCallTaxi = {},
            onReport = {},
            onLeave = {}
        )
    }
}
