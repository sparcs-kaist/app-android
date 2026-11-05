@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package org.sparcs.soap.app.features.taxiChat.rideInfo

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiPeople
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.settings.components.SettingsFieldItem
import org.sparcs.soap.app.features.settings.components.SettingsGroup
import org.sparcs.soap.app.features.settings.components.SettingsNavigationItem
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
import org.sparcs.soap.app.features.taxiChat.components.TaxiRiderCluster
import org.sparcs.soap.app.features.taxiPreview.components.RiderPhoto
import org.sparcs.soap.app.shared.extensions.formattedString
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.isDepartingSoon
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.rememberCurrentMinute
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.taxiCountdown
import org.sparcs.soap.app.theme.ui.lastSeatContainer
import org.sparcs.soap.app.theme.ui.onLastSeatContainer
import org.sparcs.soap.app.theme.ui.onSeatsOpenContainer
import org.sparcs.soap.app.theme.ui.seatsOpenContainer
import android.text.format.DateFormat as AndroidDateFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Everything about the ride in one sheet, opened from the chat's title: when and where, sharing,
 * calling a taxi and reporting, your own arrival and luggage, who else is riding — arrived first —
 * and leaving the room.
 */
@Composable
fun TaxiRideInfoSheet(
    room: TaxiRoom,
    myUserId: String?,
    isLeaveAvailable: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onArrivalChange: (Boolean) -> Unit,
    onCarrierChange: (Boolean) -> Unit,
    onCallTaxi: () -> Unit,
    onReport: () -> Unit,
    onLeave: () -> Unit,
) {
    val context = LocalContext.current
    val now by rememberCurrentMinute()
    var confirmsLeave by remember { mutableStateOf(false) }
    val me = room.participants.firstOrNull { it.id == myUserId }
    // Arrival can be marked only before the ride leaves.
    val canMarkArrival = isLeaveAvailable && room.departAt.time > now

    val shareMessage = stringResource(
        R.string.taxi_share_message,
        room.departAt.formattedString(),
        room.source.title,
        room.destination.title,
        "${Constants.TAXI_INVITE_URL}${room.id}",
    )

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Header(room, now)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                ActionTile(Icons.Outlined.Share, stringResource(R.string.share)) {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, shareMessage)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(send, null))
                }
                ActionTile(Icons.Outlined.LocalTaxi, stringResource(R.string.call_taxi), onClick = onCallTaxi)
                ActionTile(
                    Icons.Outlined.Flag,
                    stringResource(R.string.report),
                    enabled = room.participants.size > 1,
                    onClick = onReport,
                )
            }

            SettingsSectionHeader(stringResource(R.string.taxi_my_status))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                val isArrived = me?.isArrived == true
                val hasCarrier = me?.hasCarrier == true
                StatusTile(
                    isOn = isArrived,
                    onIcon = Icons.Rounded.CheckCircle,
                    offIcon = Icons.Rounded.EmojiPeople,
                    onText = stringResource(R.string.taxi_arrival_complete),
                    offText = stringResource(R.string.taxi_im_here),
                    container = MaterialTheme.colorScheme.seatsOpenContainer,
                    content = MaterialTheme.colorScheme.onSeatsOpenContainer,
                    enabled = canMarkArrival && me != null,
                ) { onArrivalChange(!isArrived) }
                StatusTile(
                    isOn = hasCarrier,
                    onIcon = Icons.Filled.Luggage,
                    offIcon = Icons.Outlined.Luggage,
                    onText = stringResource(R.string.taxi_with_luggage),
                    offText = stringResource(R.string.taxi_no_luggage),
                    container = MaterialTheme.colorScheme.primaryContainer,
                    content = MaterialTheme.colorScheme.onPrimaryContainer,
                    enabled = me != null,
                ) { onCarrierChange(!hasCarrier) }
            }
            if (!canMarkArrival) {
                Text(
                    stringResource(R.string.taxi_arrival_before_departure),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 8.dp),
                )
            }

            Riders(room.participants, myUserId)

            SettingsGroup(Modifier.padding(top = 24.dp)) {
                row {
                    SettingsNavigationItem(
                        text = stringResource(R.string.leave),
                        onClick = { confirmsLeave = true },
                        enabled = isLeaveAvailable,
                        leadingContent = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null) },
                        destructive = true,
                    )
                }
            }
        }
    }

    // Confirmed first: a stray tap at the foot of the sheet would otherwise give up your seat.
    if (confirmsLeave) {
        AlertDialog(
            onDismissRequest = { confirmsLeave = false },
            title = { Text(stringResource(R.string.leave_room_confirmation)) },
            text = { Text(stringResource(R.string.leave_room_warning)) },
            confirmButton = {
                TextButton(
                    onClick = { confirmsLeave = false; onLeave() },
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(R.string.leave)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmsLeave = false }, shapes = ButtonDefaults.shapes()) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

/** The riders' photos, the room's name and route, and when it leaves as chips. */
@Composable
private fun Header(room: TaxiRoom, now: Long) {
    val context = LocalContext.current
    val day = remember(room.departAt) {
        val pattern = AndroidDateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMdEEE")
        SimpleDateFormat(pattern, Locale.getDefault()).format(room.departAt)
    }
    val time = remember(room.departAt) { AndroidDateFormat.getTimeFormat(context).format(room.departAt) }
    val countdown = taxiCountdown(room.departAt, now)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        TaxiRiderCluster(room.participants, size = 96.dp, modifier = Modifier.padding(bottom = 8.dp))
        Text(
            "${room.title} ${room.emojiIdentifier.display}",
            style = MaterialTheme.typography.headlineSmallEmphasized,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.taxi_route, room.source.title.localized(), room.destination.title.localized()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        ) {
            DepartureChip(Icons.Outlined.CalendarToday, day)
            DepartureChip(Icons.Outlined.Schedule, time)
            if (countdown != null) {
                val soon = isDepartingSoon(room.departAt, now)
                DepartureChip(
                    Icons.Outlined.Timer,
                    countdown,
                    container = if (soon) MaterialTheme.colorScheme.lastSeatContainer else MaterialTheme.colorScheme.tertiaryContainer,
                    content = if (soon) MaterialTheme.colorScheme.onLastSeatContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}

@Composable
private fun DepartureChip(
    icon: ImageVector,
    text: String,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(shape = CircleShape, color = container, contentColor = content) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

/** One of the room's actions as a tonal tile: an icon over its name. */
@Composable
private fun RowScope.ActionTile(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f),
        modifier = Modifier.weight(1f),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
            modifier = Modifier
                .heightIn(min = 72.dp)
                .padding(vertical = 12.dp),
        ) {
            Icon(icon, contentDescription = null)
            Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

/** Your arrival or luggage as a large tile, filled with [container] when on. */
@Composable
private fun RowScope.StatusTile(
    isOn: Boolean,
    onIcon: ImageVector,
    offIcon: ImageVector,
    onText: String,
    offText: String,
    container: Color,
    content: Color,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val disabledAlpha = if (enabled) 1f else 0.38f
    Surface(
        onClick = onToggle,
        enabled = enabled,
        shape = MaterialTheme.shapes.extraLarge,
        color = if (isOn) container else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = (if (isOn) content else MaterialTheme.colorScheme.onSurface).copy(alpha = disabledAlpha),
        modifier = Modifier
            .weight(1f)
            .semantics {
                role = Role.Switch
                selected = isOn
            },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
            modifier = Modifier
                .heightIn(min = 88.dp)
                .padding(vertical = 14.dp),
        ) {
            Icon(if (isOn) onIcon else offIcon, contentDescription = null, modifier = Modifier.size(28.dp))
            Text(if (isOn) onText else offText, style = MaterialTheme.typography.titleSmall)
        }
    }
}

/**
 * Who is already at the meeting point, then who is still on the way, so a glance shows who the
 * ride is waiting for; you come first in yours.
 */
@Composable
private fun Riders(participants: List<TaxiParticipant>, myUserId: String?) {
    val sorted = participants.withIndex()
        .sortedWith(compareBy({ if (it.value.id == myUserId) 0 else 1 }, { it.index }))
        .map { it.value }
    val arrived = sorted.filter { it.isArrived }
    val onTheWay = sorted.filterNot { it.isArrived }

    if (arrived.isNotEmpty()) {
        SettingsSectionHeader(stringResource(R.string.taxi_arrived_section, arrived.size), icon = Icons.Rounded.CheckCircle)
        RiderGroup(arrived, myUserId)
    }
    if (onTheWay.isNotEmpty()) {
        SettingsSectionHeader(stringResource(R.string.taxi_on_the_way_section, onTheWay.size), icon = Icons.Outlined.DirectionsWalk)
        RiderGroup(onTheWay, myUserId)
    }
}

@Composable
private fun RiderGroup(riders: List<TaxiParticipant>, myUserId: String?) {
    SettingsGroup {
        riders.forEach { rider ->
            row {
                SettingsFieldItem {
                    ListItem(
                        leadingContent = { RiderPhoto(rider, 36.dp, showsLuggage = false) },
                        headlineContent = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(rider.nickname, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                if (rider.badge) {
                                    Icon(
                                        Icons.Outlined.VerifiedUser,
                                        contentDescription = stringResource(R.string.taxi_phone_verified),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                if (rider.id == myUserId) {
                                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                                        Text(
                                            stringResource(R.string.taxi_me),
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
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
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        } else null,
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }
        }
    }
}
