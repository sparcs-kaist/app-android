package org.sparcs.soap.app.features.taxiChat.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.theme.ui.Theme

/** Centre and diameter of each photo, as fractions of the circle, by how many riders there are. */
private fun clusterSlots(count: Int): List<Triple<Float, Float, Float>> = when (count) {
    0, 1 -> listOf(Triple(0.5f, 0.5f, 0.84f))
    2 -> listOf(Triple(0.38f, 0.38f, 0.56f), Triple(0.72f, 0.72f, 0.4f))
    3 -> listOf(Triple(0.4f, 0.38f, 0.54f), Triple(0.78f, 0.56f, 0.34f), Triple(0.46f, 0.8f, 0.3f))
    else -> listOf(
        Triple(0.38f, 0.36f, 0.5f),
        Triple(0.77f, 0.36f, 0.32f),
        Triple(0.68f, 0.74f, 0.28f),
        Triple(0.33f, 0.76f, 0.24f),
    )
}

/**
 * The riders' photos packed into one circle, the way a messaging app shows a group. Riders are grey
 * until they arrive, and arrived riders take the larger places, so the cluster fills in as people
 * turn up.
 */
@Composable
fun TaxiRiderCluster(riders: List<TaxiParticipant>, size: Dp, modifier: Modifier = Modifier) {
    val ordered = riders.filter { it.isArrived } + riders.filterNot { it.isArrived }
    val slots = clusterSlots(ordered.size)

    Box(
        modifier
            .size(size)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
            .clearAndSetSemantics { }
    ) {
        ordered.take(4).zip(slots).forEach { (rider, slot) ->
            key(rider.id) {
                val (x, y, d) = slot
                val cx by animateFloatAsState(x, MaterialTheme.motionScheme.defaultSpatialSpec(), label = "x")
                val cy by animateFloatAsState(y, MaterialTheme.motionScheme.defaultSpatialSpec(), label = "y")
                val diameter by animateFloatAsState(d, MaterialTheme.motionScheme.defaultSpatialSpec(), label = "d")
                ClusterPhoto(
                    rider = rider,
                    modifier = Modifier
                        .offset(x = size * (cx - diameter / 2), y = size * (cy - diameter / 2))
                        .size(size * diameter),
                )
            }
        }
    }
}

/** One rider's photo over a grey circle, greyed out and faded until they arrive. */
@Composable
private fun ClusterPhoto(rider: TaxiParticipant, modifier: Modifier) {
    Box(
        modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outlineVariant)
            .alpha(if (rider.isArrived) 1f else 0.55f)
    ) {
        rider.profileImageURL?.let { url ->
            AsyncImage(
                model = url.toString(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = if (rider.isArrived) null
                else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        Surface {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TaxiRoom.mockList().take(4).forEach { room -> TaxiRiderCluster(room.participants, 56.dp) }
            }
        }
    }
}
