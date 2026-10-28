package org.sparcs.soap.app.features.taxiChat.components

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.navigationBar.components.BackButton
import org.sparcs.soap.app.features.settings.components.TapTooltip
import org.sparcs.soap.app.shared.extensions.formattedString
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiTonalLabel
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.onSeatsOpenContainer
import org.sparcs.soap.app.theme.ui.seatsOpenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiChatViewNavigationBar(
    room: TaxiRoom,
    myUserId: String?,
    onDismiss: () -> Unit,
    onClickCallTaxi: () -> Unit,
    onReport: () -> Unit,
    onClickLeave: () -> Unit,
    onCarrierToggle: (Boolean) -> Unit,
    onArrivalToggle: (Boolean) -> Unit,
    isLeaveAvailable: Boolean,
) {
    val context = LocalContext.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val shareUrl = "${Constants.TAXI_INVITE_URL}${room.id}"
    val shareMessage = stringResource(
        R.string.taxi_share_message,
        room.departAt.formattedString(),
        room.source.title,
        room.destination.title,
        shareUrl
    )
    var showArrivalSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val myParticipant = room.participants.find { it.id == myUserId }
    val isMineCarrier = myParticipant?.hasCarrier ?: false
    val isArrived = myParticipant?.isArrived ?: false
    val arrivedCount = room.participants.count { it.isArrived }
    val totalCount = room.participants.size

    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            navigationIcon = { BackButton(onClick = { onDismiss() }) },
            title = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = room.title,
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.width(4.dp))

                        TapTooltip(stringResource(R.string.taxi_room_emoji_description)) {
                            Text(text = room.emojiIdentifier.display)
                        }
                    }
                    if (!isLandscape) {
                        Text(
                            text = "${room.source.title} → ${room.destination.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },

            actions = {
                TaxiChatViewDropDownMenu(
                    room = room,
                    isMineCarrier = isMineCarrier,
                    onClickShare = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareMessage)
                            type = "text/plain"
                        }

                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    onClickCallTaxi = { onClickCallTaxi() },
                    onClickReport = { onReport() },
                    onClickLeave = { onClickLeave() },
                    onCarrierToggle = onCarrierToggle,
                    isLeaveAvailable = isLeaveAvailable
                )
            }
        )

        ArrivalStatusSection(
            arrivedCount = arrivedCount,
            totalCount = totalCount,
            isArrived = isArrived,
            isEnabled = isLeaveAvailable,
            onStatusClick = { showArrivalSheet = !showArrivalSheet },
            onArrivalToggle = onArrivalToggle
        )

        HorizontalDivider()
    }

    if (showArrivalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showArrivalSheet = false },
            sheetState = sheetState
        ) {
            TaxiArrivalStatusContent(participants = room.participants)
        }
    }
}

/** How many have arrived, a button for everyone's status, and a chip to mark yourself arrived. */
@Composable
private fun ArrivalStatusSection(
    arrivedCount: Int,
    totalCount: Int,
    isArrived: Boolean,
    isEnabled: Boolean,
    onStatusClick: () -> Unit,
    onArrivalToggle: (Boolean) -> Unit,
) {
    val isAllArrived = totalCount > 0 && arrivedCount == totalCount

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Crossfade(
            targetState = when {
                isAllArrived -> pluralStringResource(R.plurals.taxi_all_arrived, totalCount, totalCount)
                arrivedCount == 0 -> stringResource(R.string.taxi_arrived_question)
                else -> pluralStringResource(R.plurals.taxi_arrived_count, arrivedCount, arrivedCount)
            },
            label = "ArrivalText",
            modifier = Modifier.weight(1f)
        ) { targetText ->
            Text(text = targetText, style = MaterialTheme.typography.bodyMedium)
        }

        TextButton(onClick = onStatusClick) {
            Text(stringResource(R.string.taxi_status))
        }

        if (isEnabled) {
            ElevatedFilterChip(
                selected = isArrived,
                onClick = { onArrivalToggle(!isArrived) },
                label = {
                    Text(stringResource(if (isArrived) R.string.taxi_arrival_complete else R.string.taxi_arrival))
                },
                leadingIcon = if (isArrived) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                } else null
            )
        }
    }
}

/** Everyone in the room and whether they have arrived. */
@Composable
fun TaxiArrivalStatusContent(participants: List<TaxiParticipant>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.taxi_arrival_status),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(participants) { participant ->
                ListItem(
                    headlineContent = { Text(participant.nickname) },
                    trailingContent = {
                        if (participant.isArrived) {
                            TaxiTonalLabel(
                                text = stringResource(R.string.taxi_arrival_complete),
                                container = MaterialTheme.colorScheme.seatsOpenContainer,
                                content = MaterialTheme.colorScheme.onSeatsOpenContainer,
                                icon = Icons.Filled.Check
                            )
                        } else {
                            TaxiTonalLabel(
                                text = stringResource(R.string.taxi_not_arrived),
                                container = MaterialTheme.colorScheme.surfaceContainerHighest,
                                content = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}


@Composable
@Preview
private fun Preview() {
    Theme {
        Box(Modifier.fillMaxSize()) {
            TaxiChatViewNavigationBar(
                TaxiRoom.mock(),
                "",
                {},
                {},
                {},
                {},
                {},
                {},
                true
            )
        }
    }
}
