package org.sparcs.soap.app.shared.views.taxiRoomCell.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun TaxiRiderAvatar(
    participant: TaxiParticipant,
    size: Dp,
    modifier: Modifier = Modifier,
    showsLuggage: Boolean = true,
) {
    Box(modifier) {
        TaxiProfilePhoto(url = participant.profileImageURL?.toString(), modifier = Modifier.size(size))

        if (showsLuggage && participant.hasCarrier) {
            Icon(
                Icons.Filled.Luggage,
                contentDescription = stringResource(R.string.taxi_with_luggage),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = size * 0.1f, y = size * 0.1f)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(size * 0.08f)
                    .size(size * 0.3f)
            )
        }
    }
}

@Composable
fun TaxiRiderCardMenu(participant: TaxiParticipant, expanded: Boolean, onDismiss: () -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        TaxiRiderCard(participant)
    }
}

@Composable
fun TaxiRiderCard(participant: TaxiParticipant, modifier: Modifier = Modifier) {
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .width(260.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TaxiRiderAvatar(participant, size = 44.dp)
            Text(
                text = participant.nickname,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        HorizontalDivider()

        if (participant.badge) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                RiderFact(Icons.Filled.VerifiedUser, stringResource(R.string.taxi_phone_verified), MaterialTheme.colorScheme.primary)
                Text(
                    text = stringResource(R.string.members_with_this_badge),
                    style = MaterialTheme.typography.bodySmall,
                    color = secondary
                )
            }
        } else {
            RiderFact(Icons.Outlined.Shield, stringResource(R.string.taxi_not_verified), secondary, textColor = secondary)
        }

        if (participant.hasCarrier) {
            RiderFact(Icons.Filled.Luggage, stringResource(R.string.taxi_with_luggage), MaterialTheme.colorScheme.primary)
        } else {
            RiderFact(Icons.Outlined.Luggage, stringResource(R.string.taxi_no_luggage), secondary, textColor = secondary)
        }
    }
}

@Composable
private fun RiderFact(
    icon: ImageVector,
    text: String,
    iconTint: Color,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = textColor)
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        val rider = TaxiRoom.mock().participants.first()
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                TaxiRiderCard(rider.copy(hasCarrier = true))
                TaxiRiderCard(rider.copy(badge = false, profileImageURL = null))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
                    TaxiRiderAvatar(rider, size = 30.dp)
                    TaxiRiderAvatar(rider.copy(hasCarrier = true), size = 30.dp)
                }
            }
        }
    }
}
