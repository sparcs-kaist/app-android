package org.sparcs.Features.TaxiRoomCreation.Components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.Domain.Models.Taxi.TaxiLocation
import org.sparcs.R
import org.sparcs.Shared.Extensions.LocalizedText
import org.sparcs.Shared.Mocks.mock
import org.sparcs.Shared.Mocks.mockList
import org.sparcs.ui.theme.Theme
import org.sparcs.ui.theme.gray64
import org.sparcs.ui.theme.lightGray0

@Composable
fun TaxiDestinationPicker(
    source: TaxiLocation?,
    onSourceChange: (TaxiLocation?) -> Unit,
    destination: TaxiLocation?,
    onDestinationChange: (TaxiLocation?) -> Unit,
    locations: List<TaxiLocation>
) {
    var isFlipped by remember { mutableStateOf(false) }
    val rotationXState = animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing)
    )

    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            LocationMenu(
                title = stringResource(R.string.meeting_point),
                selection = source,
                onSelectionChange = onSourceChange,
                locations = locations
            )

            HorizontalDivider()

            LocationMenu(
                title = stringResource(R.string.where_to),
                selection = destination,
                onSelectionChange = onDestinationChange,
                locations = locations
            )
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.lightGray0)
                .graphicsLayer {
                    rotationX = rotationXState.value
                    cameraDistance = 12 * density
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
                tint = MaterialTheme.colorScheme.primary
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
            text = (selection?.title ?: title).toString(),
            color = if (selection != null)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.gray64
        )

        Icon(
            painter = painterResource(R.drawable.round_swap_vert),
            contentDescription = "Expand dropdown"
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
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
@Preview(showBackground = true)
private fun Preview(){
    Theme {
        TaxiDestinationPicker(
            source = TaxiLocation.mock(),
            onSourceChange = { },
            destination = TaxiLocation.mock(),
            onDestinationChange = { },
            locations = TaxiLocation.mockList()
        )
    }
}