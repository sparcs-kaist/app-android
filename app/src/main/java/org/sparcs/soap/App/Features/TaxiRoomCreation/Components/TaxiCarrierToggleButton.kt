package org.sparcs.soap.App.Features.TaxiRoomCreation.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.R

@Composable
fun TaxiCarrierToggleButton(
    hasCarrier: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.carrier_status_title),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (hasCarrier) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                .clickable { onToggle(!hasCarrier) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (hasCarrier)
                    stringResource(R.string.carrier_status_yes)
                else
                    stringResource(R.string.carrier_status_no),
                style = MaterialTheme.typography.labelLarge,
                color = if (hasCarrier)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

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