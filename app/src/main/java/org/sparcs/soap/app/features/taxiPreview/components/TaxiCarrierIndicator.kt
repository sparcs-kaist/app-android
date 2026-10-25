package org.sparcs.soap.app.features.taxiPreview.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiTonalLabel
import org.sparcs.soap.app.theme.ui.Theme

/** How many people bring luggage, in the same label as the seat count; hidden when nobody does. */
@Composable
fun TaxiCarrierIndicator(
    carrierCount: Int
) {
    if (carrierCount > 0) {
        TaxiTonalLabel(
            text = carrierCount.toString(),
            container = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.onSecondaryContainer,
            icon = Icons.Outlined.Luggage
        )
    }
}

@Composable
@Preview
private fun Preview(){
    Theme {
        TaxiCarrierIndicator(1)
    }
}
