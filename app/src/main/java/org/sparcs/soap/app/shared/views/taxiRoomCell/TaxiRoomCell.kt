package org.sparcs.soap.app.shared.views.taxiRoomCell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.WhereToVote
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.features.lectureDetail.components.SkeletonLine
import org.sparcs.soap.app.shared.extensions.adaptiveIconSize
import org.sparcs.soap.app.shared.extensions.relativeTimeString
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiParticipantsIndicator
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiRoomStatusIndicator
import org.sparcs.soap.app.theme.ui.Theme

/**
 * A room as a card: where it leaves from and goes to, the riders (or, once departed, the
 * settlement status) at the top right, and the departure time and title underneath.
 */
@Composable
fun TaxiRoomCell(
    room: TaxiRoom,
    taxiUser: TaxiUser? = null,
    onClick: () -> Unit = {},
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RouteLine(Icons.Filled.NearMe, room.source.title.localized())
                    RouteLine(Icons.Filled.WhereToVote, room.destination.title.localized())
                }
                if (room.isDeparted) {
                    TaxiRoomStatusIndicator(
                        settlementType = room.participants.firstOrNull { it.id == taxiUser?.oid }?.isSettlement
                            ?: TaxiParticipant.SettlementType.NotDeparted,
                        settlementCount = room.settlementTotal ?: 0,
                        participantsCount = room.participants.size
                    )
                } else {
                    TaxiParticipantsIndicator(
                        participants = room.participants.size,
                        capacity = room.capacity
                    )
                }
            }
            Text(
                text = "${room.departAt.relativeTimeString()} • ${room.title}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RouteLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Placeholder with the same layout and text styles as [TaxiRoomCell]. */
@Composable
fun TaxiRoomSkeletonCell() {
    val typography = MaterialTheme.typography
    val tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(2) { line ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(24.dp).background(tint, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            SkeletonLine(typography.titleMedium, Modifier.fillMaxWidth(if (line == 0) 0.55f else 0.7f))
                        }
                    }
                }
                Box(Modifier.size(width = 56.dp, height = 28.dp).background(tint, MaterialTheme.shapes.small))
            }
            SkeletonLine(typography.bodyMedium, Modifier.fillMaxWidth(0.5f))
        }
    }
}

@Composable
fun IconText(icon: ImageVector, text: String) {

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.adaptiveIconSize(MaterialTheme.typography.bodyLarge),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.padding(2.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TaxiRoomCell(room = TaxiRoom.mockList()[0], taxiUser = TaxiUser.mock(), onClick = {})
            TaxiRoomSkeletonCell()
        }
    }
}