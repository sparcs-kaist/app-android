package org.sparcs.soap.app.features.taxiChat.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiProfilePhoto
import org.sparcs.soap.app.theme.ui.Theme

private data class Slot(val x: Float, val y: Float, val diameter: Float)

private fun layoutFor(count: Int): List<Slot> = when (count) {
    0, 1 -> listOf(Slot(0.5f, 0.5f, 0.84f))
    2 -> listOf(Slot(0.38f, 0.38f, 0.56f), Slot(0.72f, 0.72f, 0.4f))
    3 -> listOf(Slot(0.4f, 0.38f, 0.54f), Slot(0.78f, 0.56f, 0.34f), Slot(0.46f, 0.8f, 0.3f))
    else -> listOf(Slot(0.38f, 0.36f, 0.5f), Slot(0.77f, 0.36f, 0.32f), Slot(0.68f, 0.74f, 0.28f), Slot(0.33f, 0.76f, 0.24f))
}

@Composable
fun TaxiRiderCluster(riders: List<TaxiParticipant>, size: Dp, modifier: Modifier = Modifier) {
    val ordered = riders.filter { it.isArrived } + riders.filterNot { it.isArrived }
    val slots = layoutFor(ordered.size)

    Box(
        modifier
            .size(size)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
    ) {
        ordered.take(slots.size).forEachIndexed { index, rider ->
            key(rider.id) {
                ClusterPhoto(rider, slots[index], size)
            }
        }
    }
}

@Composable
private fun ClusterPhoto(rider: TaxiParticipant, slot: Slot, size: Dp) {
    val diameter by animateDpAsState(size * slot.diameter, label = "photoSize")
    val x by animateDpAsState(size * slot.x - size * slot.diameter / 2, label = "photoX")
    val y by animateDpAsState(size * slot.y - size * slot.diameter / 2, label = "photoY")

    TaxiProfilePhoto(
        url = rider.profileImageURL?.toString(),
        isGrayscale = !rider.isArrived,
        modifier = Modifier
            .offset(x = x, y = y)
            .size(diameter)
            .alpha(if (rider.isArrived) 1f else 0.55f)
    )
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        val riders = TaxiRoom.mock().participants
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
            TaxiRiderCluster(emptyList(), size = 48.dp)
            TaxiRiderCluster(riders.take(1), size = 48.dp)
            TaxiRiderCluster(listOf(riders[0].copy(isArrived = true), riders[1]), size = 48.dp)
            TaxiRiderCluster(riders + riders.map { it.copy(id = it.id + "x") }, size = 96.dp)
        }
    }
}
