package org.sparcs.soap.App.Features.TaxiPreview.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.App.Domain.Enums.Taxi.TaxiInfoItem
import org.sparcs.soap.App.theme.ui.Theme

@Composable
fun TaxiInfoSection(
    items: List<TaxiInfoItem>
) {
    LazyColumn(verticalArrangement = Arrangement.SpaceBetween){
        items(
            items = items,
            key = { item -> item.id }
        ) { item ->

            when (item) {
                is TaxiInfoItem.Plain -> {
                    InfoRow(label = item.label, value = item.value)
                }
                is TaxiInfoItem.WithIcon -> {
                    InfoRow(label = item.label, value = item.value, trailingIcon = item.systemImage)
                }
            }
            Spacer(Modifier.padding(4.dp))
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        TaxiInfoSection(
            listOf(
                TaxiInfoItem.Plain(label = "Test Label 1", value = "Test Value 1"),
                TaxiInfoItem.WithIcon(label = "Test Label 2", value = "Test Value 2", systemImage = Icons.Default.Add)
            )
        )
    }
}