package com.example.soap.Features.TaxiPreview.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme

@Composable
fun TaxiInfoSection(
    items: List<TaxiInfoItem>,
) {
    LazyColumn{
        items(
            items = items,
            key = { item -> item.id }
        ) { item ->

            when (item) {
                is TaxiInfoItem.plain -> {
                    InfoRow(label = item.label, value = item.value)
                }
                is TaxiInfoItem.withIcon -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoRow(
                            label = item.label,
                            value = item.value
                        )
                        Icon(
                            painter = painterResource(item.systemImage),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

}

@Composable
@Preview
private fun Preview() {
    SoapTheme {
        TaxiInfoSection(
            listOf(
                TaxiInfoItem.plain(label = "Test Label 1", value = "Test Value 1"),
                TaxiInfoItem.withIcon(label = "Test Label 2", value = "Test Value 2", systemImage = R.drawable.arrow_forward_ios)
            )
        )
    }
}