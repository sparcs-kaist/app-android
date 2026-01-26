package org.sparcs.soap.App.Features.TaxiRoomCreation.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.R

@Composable
fun TaxiCapacityPicker(
    capacity: Int,
    onCapacityChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.capacity),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.weight(1f))

        Box{
            Row(Modifier.clickable { expanded = true }) {
                Text(
                    text = stringResource(R.string.people_count, capacity),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.grayBB
                )

                Spacer(Modifier.padding(2.dp))

                Icon(
                    imageVector = Icons.Rounded.SwapVert,
                    contentDescription = "Expand dropdown",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.grayBB
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                (2..4).forEach { count ->
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.people_count, count)) },
                        onClick = {
                            onCapacityChange(count)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
@Composable
@Preview
private fun Preview(){
    Theme { TaxiCapacityPicker(4, {}) }
}