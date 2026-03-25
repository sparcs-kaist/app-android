package org.sparcs.soap.App.Features.TaxiChat.Components

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import org.sparcs.soap.App.Domain.Helpers.Constants
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiParticipant
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Features.NavigationBar.Components.DismissButton
import org.sparcs.soap.App.Shared.Extensions.formattedString
import org.sparcs.soap.App.Shared.Mocks.Taxi.mock
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.R

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
    isEnabled: Boolean,
) {
    val context = LocalContext.current
    val shareUrl = "${Constants.taxiInviteURL}${room.id}"
    val shareMessage = stringResource(
        R.string.taxi_share_message,
        room.departAt.formattedString(),
        room.source.title,
        room.destination.title,
        shareUrl
    )
    var showPopover by remember { mutableStateOf(false) }
    var showArrivalSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val myParticipant = room.participants.find { it.id == myUserId }
    val isMineCarrier = myParticipant?.hasCarrier ?: false
    val isArrived = myParticipant?.isArrived ?: false
    val arrivedCount = room.participants.count { it.isArrived }
    val totalCount = room.participants.size

    var titleHeightDp by remember { mutableStateOf(64.dp) }
    val density = LocalDensity.current

    Column {
        CenterAlignedTopAppBar(
            modifier = Modifier.height(titleHeightDp),
            navigationIcon = { DismissButton(onClick = { onDismiss() }) },
            title = {
                Column(
                    Modifier.onGloballyPositioned { coordinates ->
                        val heightInDp = with(density) { coordinates.size.height.toDp() }
                        val newTitleHeight = heightInDp + 56.dp
                        if (titleHeightDp != newTitleHeight) {
                            titleHeightDp = newTitleHeight
                        }
                    },
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(room.title)

                        Spacer(Modifier.padding(4.dp))

                        Text(
                            text = room.emojiIdentifier.display,
                            modifier = Modifier
                                .clickable { showPopover = true }
                        )
                    }
                    Text(
                        text = "${room.source.title} → ${room.destination.title}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
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
                    isEnabled = isEnabled
                )
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

        ArrivalStatusSection(
            arrivedCount = arrivedCount,
            totalCount = totalCount,
            isArrived = isArrived,
            isEnabled = isEnabled,
            onStatusClick = { showArrivalSheet = !showArrivalSheet },
            onArrivalToggle = onArrivalToggle
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
    }

    if (showArrivalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showArrivalSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            TaxiArrivalStatusContent(participants = room.participants)
        }
    }

    if (showPopover) {
        Popup(
            alignment = Alignment.TopCenter,
            onDismissRequest = { showPopover = false }
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp,
                modifier = Modifier.width(250.dp)
            ) {
                Text(
                    text = stringResource(R.string.taxi_room_emoji_description),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

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

    val buttonBackgroundColor by animateColorAsState(
        targetValue = if (isArrived) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
        label = "ButtonBackground"
    )
    val buttonContentColor by animateColorAsState(
        targetValue = if (isArrived) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
        label = "ButtonContent"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Crossfade(
                targetState = when {
                    isAllArrived -> stringResource(R.string.taxi_all_arrived, totalCount)
                    arrivedCount == 0 -> stringResource(R.string.taxi_arrived_question)
                    else -> stringResource(R.string.taxi_arrived_count, arrivedCount)
                },
                label = "ArrivalText"
            ) { targetText ->
                Text(
                    text = targetText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onStatusClick,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.height(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.taxi_status),
                            modifier = Modifier.padding(horizontal = 12.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                if (isEnabled) {
                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = { onArrivalToggle(!isArrived) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonBackgroundColor,
                            contentColor = buttonContentColor
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .animateContentSize()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            AnimatedContent(
                                targetState = if (isArrived) stringResource(R.string.taxi_arrival_complete) else stringResource(
                                    R.string.taxi_arrival
                                ),
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "ButtonText"
                            ) { targetText ->
                                Text(
                                    text = targetText,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaxiArrivalStatusContent(participants: List<TaxiParticipant>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.taxi_arrival_status),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(participants) { participant ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = participant.nickname,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = if (participant.isArrived) stringResource(R.string.taxi_arrival_complete) else stringResource(
                            R.string.taxi_not_arrived
                        ),
                        color = if (participant.isArrived) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}


@Composable
@Preview
private fun Preview() {
    //상단 패딩은 무시하시면 됩니다.
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
