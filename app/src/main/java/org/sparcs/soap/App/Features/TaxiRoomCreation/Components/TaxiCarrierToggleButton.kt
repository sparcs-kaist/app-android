package org.sparcs.soap.App.Features.TaxiRoomCreation.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.carrier_status_title),
            style = MaterialTheme.typography.titleMedium
        )

        Surface(
            onClick = { onToggle(!hasCarrier) },
            shape = RoundedCornerShape(12.dp),
            color = if (hasCarrier)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
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
}

@Composable
@Preview
private fun Preview() {
    Theme {
        TaxiCarrierToggleButton(
            hasCarrier = true,
            onToggle = {}
        )
    }
}