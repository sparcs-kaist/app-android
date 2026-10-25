package org.sparcs.soap.app.shared.views.taxiRoomCell.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.lastSeatContainer
import org.sparcs.soap.app.theme.ui.onLastSeatContainer
import org.sparcs.soap.app.theme.ui.onSeatsOpenContainer
import org.sparcs.soap.app.theme.ui.seatsOpenContainer

/** Riders out of capacity, in a tonal label: green with seats open, yellow with one left, red once full. */
@Composable
fun TaxiParticipantsIndicator(
    participants: Int,
    capacity: Int
) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (capacity - participants) {
        0 -> scheme.errorContainer to scheme.onErrorContainer
        1 -> scheme.lastSeatContainer to scheme.onLastSeatContainer
        else -> scheme.seatsOpenContainer to scheme.onSeatsOpenContainer
    }
    TaxiTonalLabel("$participants/$capacity", container, content, icon = Icons.Outlined.Group)
}

/** A small rounded label with optional icon, in a container colour and its matching content colour. */
@Composable
internal fun TaxiTonalLabel(text: String, container: Color, content: Color, icon: ImageVector? = null) {
    Surface(shape = MaterialTheme.shapes.small, color = container, contentColor = content) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview(){
    Theme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
            TaxiParticipantsIndicator(1, 4)
            TaxiParticipantsIndicator(3, 4)
            TaxiParticipantsIndicator(4, 4)
        }
    }
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun DarkPreview(){
    Theme(darkTheme = true) {
        Surface {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
                TaxiParticipantsIndicator(1, 4)
                TaxiParticipantsIndicator(3, 4)
                TaxiParticipantsIndicator(4, 4)
            }
        }
    }
}