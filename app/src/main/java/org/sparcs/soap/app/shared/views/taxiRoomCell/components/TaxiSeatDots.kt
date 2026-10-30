package org.sparcs.soap.app.shared.views.taxiRoomCell.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.creditCompleteColor
import org.sparcs.soap.app.theme.ui.creditIncompleteColor

@Composable
fun seatAvailabilityColor(seatsLeft: Int): Color = when {
    seatsLeft <= 0 -> MaterialTheme.colorScheme.error
    seatsLeft == 1 -> creditIncompleteColor
    else -> creditCompleteColor
}

@Composable
fun TaxiSeatDots(taken: Int, capacity: Int, modifier: Modifier = Modifier) {
    val tint = seatAvailabilityColor(capacity - taken)
    val empty = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
    val label = stringResource(R.string.taxi_seat_count, taken, capacity)

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.clearAndSetSemantics { contentDescription = label }
    ) {
        repeat(capacity.coerceAtLeast(0)) { index ->
            Box(
                Modifier
                    .size(7.dp)
                    .background(if (index < taken) tint else empty, CircleShape)
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
            TaxiSeatDots(taken = 1, capacity = 4)
            TaxiSeatDots(taken = 3, capacity = 4)
            TaxiSeatDots(taken = 4, capacity = 4)
        }
    }
}
