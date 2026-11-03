package org.sparcs.soap.app.features.taxiChat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.LocalTaxi
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.shared.extensions.formattedString
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.views.contentViews.ExpressiveMenu
import org.sparcs.soap.app.shared.views.contentViews.MenuGroup
import org.sparcs.soap.app.shared.views.contentViews.menuItemShape
import org.sparcs.soap.app.theme.ui.Theme


/**
 * The chat's overflow menu: share and call a taxi; the departure time and participants;
 * a switch for bringing luggage; and reporting or leaving the room, after a confirmation.
 */
@Composable
fun TaxiChatViewDropDownMenu(
    room: TaxiRoom,
    isMineCarrier: Boolean,
    onClickShare: () -> Unit,
    onClickCallTaxi: () -> Unit,
    onClickReport: () -> Unit,
    onClickLeave: () -> Unit,
    onCarrierToggle: (Boolean) -> Unit,
    isLeaveAvailable: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = !expanded }, shapes = IconButtonDefaults.shapes()) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.more)
        )
    }

    ExpressiveMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        TaxiChatMenuItems(
            room = room,
            isMineCarrier = isMineCarrier,
            onClickShare = { expanded = false; onClickShare() },
            onClickCallTaxi = { expanded = false; onClickCallTaxi() },
            onClickReport = { expanded = false; onClickReport() },
            onClickLeave = { expanded = false; showLeaveDialog = true },
            onCarrierToggle = onCarrierToggle,
            isLeaveAvailable = isLeaveAvailable
        )
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text(stringResource(R.string.leave_room_confirmation)) },
            text = { Text(stringResource(R.string.leave_room_warning)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        onClickLeave()
                    },
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.leave)) }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }, shapes = ButtonDefaults.shapes()) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun TaxiChatMenuItems(
    room: TaxiRoom,
    isMineCarrier: Boolean,
    onClickShare: () -> Unit,
    onClickCallTaxi: () -> Unit,
    onClickReport: () -> Unit,
    onClickLeave: () -> Unit,
    onCarrierToggle: (Boolean) -> Unit,
    isLeaveAvailable: Boolean
) {
    var showParticipants by remember { mutableStateOf(false) }

    // Groups: room actions, the room's details, and reporting or leaving last.
    MenuGroup(index = 0, count = 3) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.share)) },
            leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
            onClick = onClickShare,
            shape = menuItemShape(0, 2)
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.call_taxi)) },
            leadingIcon = { Icon(Icons.Outlined.LocalTaxi, contentDescription = null) },
            onClick = onClickCallTaxi,
            shape = menuItemShape(1, 2)
        )
    }

    MenuGroup(index = 1, count = 3) {
        val participantCount = if (showParticipants) room.participants.size else 0
        val detailCount = 3 + participantCount
        // The departure time is information, not an action.
        DropdownMenuItem(
            text = { Text(room.departAt.formattedString()) },
            leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
            onClick = {},
            shape = menuItemShape(0, detailCount),
            enabled = false
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.room_participants, room.participants.size, room.capacity)) },
            leadingIcon = { Icon(Icons.Outlined.Group, contentDescription = null) },
            trailingContent = {
                Icon(if (showParticipants) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
            },
            onClick = { showParticipants = !showParticipants },
            shape = menuItemShape(1, detailCount)
        )
        AnimatedVisibility(visible = showParticipants) {
            Column {
                room.participants.forEachIndexed { index, participant ->
                    DropdownMenuItem(
                        text = { Text(participant.nickname) },
                        // Indented under the participants row, with a mark for who brings luggage.
                        leadingIcon = { Spacer(Modifier.width(24.dp)) },
                        trailingContent = if (participant.hasCarrier) {
                            { Icon(Icons.Outlined.Luggage, contentDescription = stringResource(R.string.carrier_status_title)) }
                        } else null,
                        onClick = {},
                        shape = menuItemShape(2 + index, 3 + room.participants.size),
                        enabled = false
                    )
                }
            }
        }
        DropdownMenuItem(
            text = { Text(stringResource(R.string.carrier_status_title)) },
            leadingIcon = { Icon(Icons.Outlined.Luggage, contentDescription = null) },
            trailingContent = { Switch(checked = isMineCarrier, onCheckedChange = null) },
            onClick = { onCarrierToggle(!isMineCarrier) },
            shape = menuItemShape(detailCount - 1, detailCount),
            modifier = Modifier.semantics { role = Role.Switch }
        )
    }

    MenuGroup(index = 2, count = 3) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.report)) },
            leadingIcon = { Icon(Icons.Outlined.Report, contentDescription = null) },
            onClick = onClickReport,
            shape = menuItemShape(0, 2),
            enabled = room.participants.size > 1
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.leave)) },
            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null) },
            onClick = onClickLeave,
            shape = menuItemShape(1, 2),
            enabled = isLeaveAvailable,
            colors = MenuDefaults.itemColors(
                textColor = MaterialTheme.colorScheme.error,
                leadingIconColor = MaterialTheme.colorScheme.error
            )
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.width(280.dp)) {
            Column {
                TaxiChatMenuItems(room = TaxiRoom.mock(), isMineCarrier = true, {}, {}, {}, {}, {}, isLeaveAvailable = true)
            }
        }
    }
}
