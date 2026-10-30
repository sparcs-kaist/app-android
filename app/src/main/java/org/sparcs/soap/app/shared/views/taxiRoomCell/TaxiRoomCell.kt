package org.sparcs.soap.app.shared.views.taxiRoomCell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.views.contentViews.SkeletonLine
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiDepartureDetail
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiRiderStack
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiRoomStatusIndicator
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiRouteStops
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiSeatDots
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.rememberTaxiClock
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.taxiTime
import org.sparcs.soap.app.theme.ui.Theme

private val SideBySideMinWidth = 300.dp
private val TimeColumnWidth = 88.dp

@Composable
fun TaxiRoomCell(
    room: TaxiRoom,
    taxiUser: TaxiUser? = null,
    showsRiders: Boolean = false,
    onClick: () -> Unit = {},
) {
    val now = rememberTaxiClock()
    val showsStatus = room.isDeparted && taxiUser != null

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        BoxWithConstraints(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            val isSideBySide = maxWidth >= SideBySideMinWidth

            val departure: @Composable () -> Unit = {
                Text(
                    text = room.departAt.taxiTime(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                TaxiDepartureDetail(room.departAt, room.isDeparted, now)
            }
            val status: @Composable () -> Unit = { TaxiRoomStatusIndicator(room, taxiUser?.oid) }

            if (isSideBySide) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    Column(
                        verticalArrangement = if (showsStatus) Arrangement.Top else Arrangement.Center,
                        modifier = Modifier
                            .width(TimeColumnWidth)
                            .fillMaxHeight()
                    ) {
                        departure()
                        if (showsStatus) {
                            Spacer(Modifier.weight(1f).height(8.dp))
                            status()
                        }
                    }

                    VerticalDivider(Modifier.padding(vertical = 2.dp))

                    Box(Modifier.weight(1f)) {
                        RoomDetails(room, showsRiders, showsStatus)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { departure() }
                        if (showsStatus) status()
                    }
                    RoomDetails(room, showsRiders, showsStatus)
                }
            }
        }
    }
}

@Composable
private fun RoomDetails(room: TaxiRoom, showsRiders: Boolean, showsStatus: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TaxiRouteStops(source = room.source.title.localized(), destination = room.destination.title.localized())

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = room.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            when {
                showsRiders -> TaxiRiderStack(room.participants, room.capacity, showsCount = !showsStatus)
                !showsStatus -> TaxiSeatDots(room.participants.size, room.capacity)
            }
        }
    }
}

@Composable
fun TaxiRoomSkeletonCell() {
    val typography = MaterialTheme.typography
    val tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    Card(Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(IntrinsicSize.Min)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .width(TimeColumnWidth)
                    .fillMaxHeight()
            ) {
                SkeletonLine(typography.titleLarge, Modifier.fillMaxWidth(0.8f))
                SkeletonLine(typography.labelMedium, Modifier.fillMaxWidth(0.6f))
            }

            VerticalDivider(Modifier.padding(vertical = 2.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(2) { line ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(11.dp)
                        ) {
                            Box(Modifier.size(14.dp).background(tint, CircleShape))
                            SkeletonLine(typography.titleMedium, Modifier.fillMaxWidth(if (line == 0) 0.55f else 0.7f))
                        }
                    }
                }
                SkeletonLine(typography.bodyMedium, Modifier.fillMaxWidth(0.4f))
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        val rooms = TaxiRoom.mockList()
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TaxiRoomCell(room = rooms[0], taxiUser = TaxiUser.mock())
            TaxiRoomCell(room = rooms[1].copy(participants = emptyList()), taxiUser = TaxiUser.mock())
            TaxiRoomCell(room = rooms[2].copy(isDeparted = true), taxiUser = TaxiUser.mock(), showsRiders = true)
            TaxiRoomSkeletonCell()
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 300)
private fun NarrowPreview() {
    Theme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TaxiRoomCell(room = TaxiRoom.mock(), taxiUser = TaxiUser.mock(), showsRiders = true)
            TaxiRoomCell(room = TaxiRoom.mock().copy(isDeparted = true), taxiUser = TaxiUser.mock())
        }
    }
}
