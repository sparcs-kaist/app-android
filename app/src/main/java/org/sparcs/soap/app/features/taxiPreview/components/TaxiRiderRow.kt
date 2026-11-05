@file:OptIn(ExperimentalMaterial3Api::class)

package org.sparcs.soap.app.features.taxiPreview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.PhoneDisabled
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiTonalLabel
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.lastSeatContainer
import org.sparcs.soap.app.theme.ui.onLastSeatContainer
import org.sparcs.soap.app.theme.ui.onSeatsOpenContainer
import org.sparcs.soap.app.theme.ui.seatsOpenContainer

private val AvatarSize = 36.dp

/**
 * Who is riding — a photo per rider, badged when they bring luggage, and a dashed circle per open
 * seat — with how many seats remain. Tapping a photo shows who that rider is.
 */
@Composable
fun TaxiRiderRow(participants: List<TaxiParticipant>, capacity: Int, modifier: Modifier = Modifier) {
    val seatsLeft = (capacity - participants.size).coerceAtLeast(0)
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (seatsLeft) {
        0 -> scheme.errorContainer to scheme.onErrorContainer
        1 -> scheme.lastSeatContainer to scheme.onLastSeatContainer
        else -> scheme.seatsOpenContainer to scheme.onSeatsOpenContainer
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            participants.forEach { RiderAvatar(it) }
            repeat(seatsLeft) { OpenSeat() }
        }
        Spacer(Modifier.weight(1f))
        TaxiTonalLabel(
            text = if (seatsLeft == 0) stringResource(R.string.taxi_seats_full)
            else pluralStringResource(R.plurals.taxi_seats_left, seatsLeft, seatsLeft),
            container = container,
            content = content,
        )
    }
}

/** A rider's photo, with their name, phone verification and luggage in a tooltip when tapped. */
@Composable
private fun RiderAvatar(participant: TaxiParticipant) {
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            RichTooltip(title = { Text(participant.nickname) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    if (participant.badge) {
                        Fact(Icons.Outlined.VerifiedUser, stringResource(R.string.taxi_phone_verified))
                        Text(
                            stringResource(R.string.members_with_this_badge),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        Fact(Icons.Outlined.PhoneDisabled, stringResource(R.string.taxi_phone_not_verified))
                    }
                    if (participant.hasCarrier) {
                        Fact(Icons.Filled.Luggage, stringResource(R.string.taxi_with_luggage))
                    } else {
                        Fact(Icons.Outlined.Luggage, stringResource(R.string.taxi_no_luggage))
                    }
                }
            }
        },
        state = tooltipState,
    ) {
        Box(
            Modifier.clickable(
                role = Role.Button,
                onClickLabel = participant.nickname,
            ) { scope.launch { tooltipState.show() } }
        ) {
            RiderPhoto(participant, AvatarSize)
        }
    }
}

@Composable
private fun Fact(icon: ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

/** A rider's photo, badged with a suitcase when they bring luggage. */
@Composable
fun RiderPhoto(participant: TaxiParticipant, size: Dp) {
    Box(Modifier.size(size)) {
        val photo = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        if (participant.profileImageURL != null) {
            AsyncImage(
                model = participant.profileImageURL.toString(),
                contentDescription = participant.nickname,
                contentScale = ContentScale.Crop,
                modifier = photo,
            )
        } else {
            Box(photo)
        }
        if (participant.hasCarrier) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = size * 0.12f, y = size * 0.12f),
            ) {
                Icon(
                    Icons.Filled.Luggage,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(size * 0.08f)
                        .size(size * 0.3f),
                )
            }
        }
    }
}

/** An open seat: a dashed circle the size of a rider's photo. */
@Composable
private fun OpenSeat() {
    val color = MaterialTheme.colorScheme.outline
    Box(
        Modifier
            .size(AvatarSize)
            .drawBehind {
                val stroke = 1.5.dp.toPx()
                drawCircle(
                    color,
                    radius = (size.minDimension - stroke) / 2,
                    style = Stroke(stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))),
                )
            }
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
                TaxiRoom.mockList().take(3).forEach { TaxiRiderRow(it.participants, it.capacity) }
            }
        }
    }
}
