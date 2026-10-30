package org.sparcs.soap.app.shared.views.taxiRoomCell.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.theme.ui.Theme

private val PhotoSize = 22.dp
private val RingWidth = 1.5.dp
private const val MAXIMUM_PHOTOS = 4

val TaxiRiderStackHeight = PhotoSize + RingWidth * 2

@Composable
fun TaxiRiderStack(
    participants: List<TaxiParticipant>,
    capacity: Int,
    modifier: Modifier = Modifier,
    showsCount: Boolean = true,
    ringColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    val label = stringResource(R.string.taxi_seat_count, participants.size, capacity)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.clearAndSetSemantics { contentDescription = label }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(-PhotoSize * 0.3f)) {
            participants.take(MAXIMUM_PHOTOS).forEach { participant ->
                TaxiProfilePhoto(
                    url = participant.profileImageURL?.toString(),
                    modifier = Modifier
                        .background(ringColor, CircleShape)
                        .padding(RingWidth)
                        .size(PhotoSize)
                )
            }
        }

        if (showsCount) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        val participants = TaxiRoom.mock().participants
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
            TaxiRiderStack(participants = participants, capacity = 4)
            TaxiRiderStack(participants = participants + participants, capacity = 4, showsCount = false)
            TaxiRiderStack(participants = emptyList(), capacity = 4)
        }
    }
}
