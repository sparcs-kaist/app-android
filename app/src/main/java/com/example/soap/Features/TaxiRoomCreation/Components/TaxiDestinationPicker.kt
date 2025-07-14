package com.example.soap.Features.TaxiRoomCreation.Components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Domain.Models.Taxi.TaxiLocation
import com.example.soap.R
import com.example.soap.Shared.Extensions.LocalizedText
import com.example.soap.Shared.Mocks.mock
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@Composable
fun TaxiDestinationPicker(
    source: TaxiLocation?,
    onSourceChange: (TaxiLocation?) -> Unit,
    destination: TaxiLocation?,
    onDestinationChange: (TaxiLocation?) -> Unit,
    locations: List<TaxiLocation>
) {
    var isFlipped by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            LocationMenu(
                title = "meeting point",
                selection = source,
                onSelectionChange = onSourceChange,
                locations = locations
            )

            HorizontalDivider()

            LocationMenu(
                title = "where to?",
                selection = destination,
                onSelectionChange = onDestinationChange,
                locations = locations
            )
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.soapColors.gray0Border)
                .graphicsLayer {
                    rotationX = if (isFlipped) 180f else 0f
                }
                .animateContentSize()
                .clickable {
                    isFlipped = !isFlipped
                    onSourceChange(destination)
                    onDestinationChange(source)
                }
                .padding(4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.round_swap_calls),
                contentDescription = "Swap",
                tint = MaterialTheme.soapColors.primary
            )
        }
    }
}



@Composable
fun LocationMenu(
    title: String,
    selection: TaxiLocation?,
    onSelectionChange: (TaxiLocation?) -> Unit,
    locations: List<TaxiLocation>
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
        .fillMaxWidth()
        .clickable { expanded = true }
        .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = (selection?.title ?: "Select location").toString(),
            color = if (selection != null)
                MaterialTheme.soapColors.onSurface
            else
                MaterialTheme.soapColors.gray0Border
        )

        Icon(
            painter = painterResource(R.drawable.round_swap_vert),
            contentDescription = "Expand dropdown"
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background(MaterialTheme.soapColors.surface)
    ) {
        DropdownMenuItem(
            text = { Text(title) },
            onClick = {
                onSelectionChange(null)
                expanded = false
            }
        )

        locations.forEach { location ->
            DropdownMenuItem(
                text = { LocalizedText(location.title) },
                onClick = {
                    onSelectionChange(location)
                    expanded = false
                }
            )
        }
    }
}


@Composable
@Preview
private fun Preview(){
    SoapTheme {
        TaxiDestinationPicker(
            source = TaxiLocation.mock(),
            onSourceChange = { },
            destination = TaxiLocation.mock(),
            onDestinationChange = { },
            locations = TaxiLocation.mockList()
        )
    }
}