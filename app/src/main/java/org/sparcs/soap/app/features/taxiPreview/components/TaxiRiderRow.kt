package org.sparcs.soap.app.features.taxiPreview.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiRiderAvatar
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiRiderCardMenu
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.seatAvailabilityColor
import org.sparcs.soap.app.theme.ui.Theme

private val AvatarSize = 30.dp

@Composable
fun TaxiRiderRow(participants: List<TaxiParticipant>, capacity: Int, modifier: Modifier = Modifier) {
    val seatsLeft = (capacity - participants.size).coerceAtLeast(0)

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            participants.forEach { participant -> RiderButton(participant) }
            repeat(seatsLeft) {
                Box(
                    Modifier
                        .size(AvatarSize)
                        .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                )
            }
        }

        Spacer(Modifier.weight(1f).width(8.dp))

        Text(
            text = if (seatsLeft == 0) stringResource(R.string.taxi_full)
            else pluralStringResource(R.plurals.taxi_seats_left, seatsLeft, seatsLeft),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = seatAvailabilityColor(seatsLeft)
        )
    }
}

@Composable
private fun RiderButton(participant: TaxiParticipant) {
    var showsCard by remember { mutableStateOf(false) }

    Box {
        TaxiRiderAvatar(
            participant = participant,
            size = AvatarSize,
            modifier = Modifier.clickable(
                onClickLabel = participant.nickname,
                role = Role.Button,
                onClick = { showsCard = true }
            )
        )
        TaxiRiderCardMenu(participant, expanded = showsCard, onDismiss = { showsCard = false })
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        val riders = TaxiRoom.mock().participants
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            TaxiRiderRow(riders, capacity = 4)
            TaxiRiderRow(riders.take(1), capacity = 3)
            TaxiRiderRow(riders, capacity = 2)
            TaxiRiderRow(emptyList(), capacity = 4)
        }
    }
}
