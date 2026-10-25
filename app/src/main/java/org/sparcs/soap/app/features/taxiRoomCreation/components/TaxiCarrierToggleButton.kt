package org.sparcs.soap.app.features.taxiRoomCreation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

/** Whether the creator is bringing a luggage carrier, as a switch row. */
@Composable
fun TaxiCarrierToggleButton(
    hasCarrier: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        ListItem(
            leadingContent = { Icon(Icons.Outlined.Luggage, contentDescription = null) },
            headlineContent = { Text(stringResource(R.string.carrier_status_title)) },
            supportingContent = {
                Text(stringResource(if (hasCarrier) R.string.carrier_status_yes else R.string.carrier_status_no))
            },
            trailingContent = { Switch(checked = hasCarrier, onCheckedChange = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.toggleable(value = hasCarrier, role = Role.Switch, onValueChange = onToggle)
        )
    }
}

@Composable
@Preview
private fun PreviewWithCarrier() {
    Theme {
        TaxiCarrierToggleButton(
            hasCarrier = true,
            onToggle = {}
        )
    }
}

@Composable
@Preview
private fun PreviewWithoutCarrier() {
    Theme {
        TaxiCarrierToggleButton(
            hasCarrier = false,
            onToggle = {}
        )
    }
}