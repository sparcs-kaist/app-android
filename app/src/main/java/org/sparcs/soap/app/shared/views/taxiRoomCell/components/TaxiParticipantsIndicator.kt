package org.sparcs.soap.app.shared.views.taxiRoomCell.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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

/** Riders out of capacity, in a tonal pill: error once full, tertiary with one seat left. */
@Composable
fun TaxiParticipantsIndicator(
    participants: Int,
    capacity: Int
) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (capacity - participants) {
        0 -> scheme.errorContainer to scheme.onErrorContainer
        1 -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        else -> scheme.secondaryContainer to scheme.onSecondaryContainer
    }
    TaxiTonalLabel("$participants/$capacity", container, content, icon = Icons.Outlined.Group)
}

/** A small pill with optional icon, in a container colour and its matching content colour. */
@Composable
internal fun TaxiTonalLabel(text: String, container: Color, content: Color, icon: ImageVector? = null) {
    Surface(shape = CircleShape, color = container, contentColor = content) {
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
@Preview
private fun Preview(){
    Theme { TaxiParticipantsIndicator(2, 3) }
}