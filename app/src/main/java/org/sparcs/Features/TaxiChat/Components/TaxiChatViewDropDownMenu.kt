package org.sparcs.Features.TaxiChat.Components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.Domain.Models.Taxi.TaxiRoom
import org.sparcs.R
import org.sparcs.Shared.Extensions.formattedString
import org.sparcs.Shared.Mocks.mock
import org.sparcs.ui.theme.Theme
import org.sparcs.ui.theme.darkGray
import org.sparcs.ui.theme.gray64
import org.sparcs.ui.theme.lightGray0


@Composable
fun TaxiChatViewDropDownMenu(
    room: TaxiRoom,
    onClickShare: () -> Unit,
    onClickCallTaxi: () -> Unit,
    onClickReport: () -> Unit,
    onClickLeave: () -> Unit,
    isEnabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = !expanded }) {
        Icon(
            painter = painterResource(R.drawable.more_horiz),
            contentDescription = "Menu",
            tint = MaterialTheme.colorScheme.darkGray,
            modifier = Modifier.size(30.dp)
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            TopDropDownItems(
                onClickShare = { onClickShare() },
                onClickCallTaxi = { onClickCallTaxi() },
                onClickReport = { onClickReport() }
            )
            MiddleDropDownItems(room = room)
            BottomDropDownItems(
                onClickLeave = { showLeaveDialog = true },
                isEnabled = isEnabled
            )
        }
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text(stringResource(R.string.leave_room_confirmation)) },
            text = { Text(stringResource(R.string.leave_room_warning)) },
            confirmButton = {
                Button(
                    onClick = {
                    showLeaveDialog = false
                    onClickLeave()
                },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.lightGray0)
                ) {
                    Text(
                        text= stringResource(R.string.yes),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLeaveDialog = false },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                    ) {
                    Text(
                        text = stringResource(R.string.no),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}

@Composable
private fun TopDropDownItems(
    onClickShare: () -> Unit,
    onClickCallTaxi: () -> Unit,
    onClickReport: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconWithText(
            icon = painterResource(R.drawable.share),
            text = stringResource(R.string.share),
            onClick = { onClickShare() }
        )
        IconWithText(
            icon = painterResource(R.drawable.taxi),
            text = stringResource(R.string.call_taxi),
            onClick = { onClickCallTaxi() }
        )
        IconWithText(
            icon = painterResource(R.drawable.round_report_problem),
            text = stringResource(R.string.report),
            onClick = { onClickReport() }
        )
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.lightGray0,
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
private fun MiddleDropDownItems(room: TaxiRoom) {
    var showParticipants by remember { mutableStateOf(false) }
    Column {
        DropdownMenuItem(
            text = {
                Text(
                    text = room.departAt.formattedString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.gray64
                )
            },
            onClick = {},
            enabled = false,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.baseline_calendar_month),
                    contentDescription = "Depart Time",
                    tint = MaterialTheme.colorScheme.darkGray,
                )
            }
        )

        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(
                        R.string.room_participants,
                        room.participants.size,
                        room.capacity
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            onClick = { showParticipants = !showParticipants },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.group),
                    contentDescription = "Participants",
                    tint = MaterialTheme.colorScheme.darkGray,
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.arrow_forward_ios),
                    contentDescription = "Toggle participants",
                    tint = MaterialTheme.colorScheme.darkGray,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(if (showParticipants) 270f else 0f)
                )
            }
        )



    AnimatedVisibility(
            visible = showParticipants,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                room.participants.forEach { participant ->
                    Text(
                        text = participant.nickname,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.gray64,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.lightGray0,
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
private fun BottomDropDownItems(
    onClickLeave: () -> Unit,
    isEnabled: Boolean
){
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(R.string.leave),
                color = MaterialTheme.colorScheme.error
            ) },
        onClick = { onClickLeave() },
        enabled = isEnabled,
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.round_logout),
                contentDescription = "Leave Room",
                tint = MaterialTheme.colorScheme.error
            )
        }
    )
}


@Composable
private fun IconWithText(
    icon: Painter,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Icon(
            painter = icon,
            contentDescription = text
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
@Preview
private fun Preview(){
    Theme {
        Box(Modifier.fillMaxSize()){
            Button(
                onClick = {}
            ) {
                TaxiChatViewDropDownMenu(room = TaxiRoom.mock(), {}, {}, {},{}, true)
            }
        }
    }
}