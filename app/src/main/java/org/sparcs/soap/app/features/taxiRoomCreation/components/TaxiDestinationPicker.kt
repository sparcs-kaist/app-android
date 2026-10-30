package org.sparcs.soap.app.features.taxiRoomCreation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.theme.ui.Theme

private val RowHeight = 56.dp
private val MarkerSize = 18.dp
private val MarkerColumnWidth = 48.dp

@Composable
fun TaxiDestinationPicker(
    source: TaxiLocation?,
    onSourceChange: (TaxiLocation?) -> Unit,
    destination: TaxiLocation?,
    onDestinationChange: (TaxiLocation?) -> Unit,
    locations: List<TaxiLocation>,
    favoritePlaceIDs: List<String> = emptyList(),
    onToggleFavoritePlace: (TaxiLocation) -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    var swapCount by remember { mutableIntStateOf(0) }
    val swapRotation by animateFloatAsState(swapCount * 180f, label = "swapRotation")

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                LocationRow(
                    placeholder = stringResource(R.string.taxi_route_from),
                    isOrigin = true,
                    selection = source,
                    onSelectionChange = onSourceChange,
                    locations = locations.filter { it.id != destination?.id },
                    favoritePlaceIDs = favoritePlaceIDs,
                    onToggleFavorite = onToggleFavoritePlace,
                    swapCount = swapCount
                )
                HorizontalDivider(Modifier.padding(start = MarkerColumnWidth))
                LocationRow(
                    placeholder = stringResource(R.string.taxi_route_to),
                    isOrigin = false,
                    selection = destination,
                    onSelectionChange = onDestinationChange,
                    locations = locations.filter { it.id != source?.id },
                    favoritePlaceIDs = favoritePlaceIDs,
                    onToggleFavorite = onToggleFavoritePlace,
                    swapCount = swapCount
                )
            }

            FilledTonalIconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    swapCount += 1
                    onSourceChange(destination)
                    onDestinationChange(source)
                },
                enabled = source != null || destination != null,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    Icons.Filled.SwapVert,
                    contentDescription = stringResource(R.string.swap_route),
                    modifier = Modifier.rotate(swapRotation)
                )
            }
        }
    }
}

@Composable
private fun LocationRow(
    placeholder: String,
    isOrigin: Boolean,
    selection: TaxiLocation?,
    onSelectionChange: (TaxiLocation?) -> Unit,
    locations: List<TaxiLocation>,
    favoritePlaceIDs: List<String>,
    onToggleFavorite: (TaxiLocation) -> Unit,
    swapCount: Int,
) {
    var expanded by remember { mutableStateOf(false) }
    val markerTint = if (selection == null) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
    val lineColor = MaterialTheme.colorScheme.outlineVariant

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(RowHeight)
                .clickable(onClickLabel = placeholder, role = Role.DropdownList) { expanded = true }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(MarkerColumnWidth)
                    .fillMaxHeight()
                    .drawBehind {
                        val x = size.width / 2
                        val gap = (MarkerSize / 2 + 3.dp).toPx()
                        val start = if (isOrigin) size.height / 2 + gap else 0f
                        val end = if (isOrigin) size.height else size.height / 2 - gap
                        drawLine(lineColor, Offset(x, start), Offset(x, end), strokeWidth = 2.dp.toPx())
                    }
            ) {
                RouteMarker(isOrigin, markerTint)
            }

            AnimatedContent(
                targetState = selection,
                transitionSpec = {
                    val direction = if (isOrigin) 1 else -1
                    (slideInVertically { it * direction } + fadeIn()) togetherWith
                        (slideOutVertically { -it * direction } + fadeOut())
                },
                contentKey = { it?.id to swapCount },
                label = "locationName",
                modifier = Modifier.weight(1f)
            ) { location ->
                LocationName(location, placeholder, isFavorite = location != null && location.id in favoritePlaceIDs)
            }

            if (selection == null) {
                Icon(
                    Icons.Filled.UnfoldMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
            AnimatedVisibility(visible = selection != null, enter = fadeIn(), exit = fadeOut()) {
                IconButton(onClick = { onSelectionChange(null) }) {
                    Icon(
                        Icons.Filled.Cancel,
                        contentDescription = stringResource(R.string.taxi_clear_place),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            LocationMenuContent(
                selection = selection,
                locations = locations,
                favoritePlaceIDs = favoritePlaceIDs,
                onToggleFavorite = onToggleFavorite,
                onSelect = {
                    onSelectionChange(it)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun LocationName(location: TaxiLocation?, placeholder: String, isFavorite: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = location?.title?.localized() ?: placeholder,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (location == null) FontWeight.Normal else FontWeight.SemiBold,
            color = if (location == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (isFavorite) {
            Icon(
                Icons.Filled.Star,
                contentDescription = stringResource(R.string.taxi_favourite_places),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun LocationMenuContent(
    selection: TaxiLocation?,
    locations: List<TaxiLocation>,
    favoritePlaceIDs: List<String>,
    onToggleFavorite: (TaxiLocation) -> Unit,
    onSelect: (TaxiLocation) -> Unit,
) {
    val favorites = locations.filter { it.id in favoritePlaceIDs }
    val (popular, others) = remember(locations, favoritePlaceIDs) {
        TaxiLocation.popularFirst(locations.filterNot { it.id in favoritePlaceIDs })
    }

    if (selection != null) {
        val isFavorite = selection.id in favoritePlaceIDs
        DropdownMenuItem(
            text = {
                Text(stringResource(if (isFavorite) R.string.taxi_remove_from_favourites else R.string.taxi_add_to_favourites))
            },
            leadingIcon = { Icon(if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline, contentDescription = null) },
            onClick = { onToggleFavorite(selection) }
        )
    }

    listOf(favorites, popular, others)
        .filter { it.isNotEmpty() }
        .forEachIndexed { index, group ->
            if (index > 0 || selection != null) HorizontalDivider()
            group.forEach { location ->
                LocationMenuItem(
                    location = location,
                    isSelected = location.id == selection?.id,
                    isFavorite = location.id in favoritePlaceIDs,
                    onClick = { onSelect(location) }
                )
            }
        }
}

@Composable
private fun LocationMenuItem(
    location: TaxiLocation,
    isSelected: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(location.title.localized()) },
        leadingIcon = if (isFavorite) {
            { Icon(Icons.Filled.Star, contentDescription = stringResource(R.string.taxi_favourite_places)) }
        } else null,
        trailingIcon = if (isSelected) {
            { Icon(Icons.Filled.Check, contentDescription = null) }
        } else null,
        onClick = onClick
    )
}

@Composable
private fun RouteMarker(isOrigin: Boolean, tint: Color) {
    if (isOrigin) {
        Icon(Icons.Filled.MyLocation, contentDescription = null, tint = tint, modifier = Modifier.size(MarkerSize))
    } else {
        Box(Modifier.size(MarkerSize), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(MarkerSize * 0.6f)
                    .background(tint, CircleShape)
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        val locations = TaxiLocation.mockList()
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            TaxiDestinationPicker(
                source = locations[0],
                onSourceChange = {},
                destination = locations[1],
                onDestinationChange = {},
                locations = locations,
                favoritePlaceIDs = listOf(locations[1].id)
            )
            TaxiDestinationPicker(
                source = null,
                onSourceChange = {},
                destination = null,
                onDestinationChange = {},
                locations = locations
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun MenuPreview() {
    Theme {
        val locations = TaxiLocation.mockList()
        Column {
            LocationMenuContent(
                selection = locations[0],
                locations = locations,
                favoritePlaceIDs = listOf(locations[2].id),
                onToggleFavorite = {},
                onSelect = {}
            )
        }
    }
}
