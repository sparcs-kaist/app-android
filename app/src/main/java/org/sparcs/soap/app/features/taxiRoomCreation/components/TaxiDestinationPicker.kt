package org.sparcs.soap.app.features.taxiRoomCreation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemShapes
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiFavoriteRoute
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import org.sparcs.soap.app.shared.extensions.LocalizedText
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.views.contentViews.ExpressiveMenu
import org.sparcs.soap.app.shared.views.contentViews.MenuGroup
import org.sparcs.soap.app.shared.views.contentViews.selectableMenuItemColors
import org.sparcs.soap.app.theme.ui.Theme

/**
 * The route as a card, marked like the route on a room card: the location arrow for the meeting
 * point, a dot for the destination and a line between them. Each row opens its places whether or
 * not one is chosen, a chosen place can be cleared beside it, and the favourite routes sit below.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TaxiDestinationPicker(
    source: TaxiLocation?,
    onSourceChange: (TaxiLocation?) -> Unit,
    destination: TaxiLocation?,
    onDestinationChange: (TaxiLocation?) -> Unit,
    locations: List<TaxiLocation>,
    favoriteRoutes: List<TaxiFavoriteRoute> = emptyList(),
    onFavoriteSelect: (TaxiFavoriteRoute) -> Unit = {},
    onFavoriteDelete: (String) -> Unit = {},
    onFavoriteAdd: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val canAddFavorite = source != null && destination != null && source.id != destination.id &&
            favoriteRoutes.none { it.from.id == source.id && it.to.id == destination.id }

    var column by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var sourceMarker by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var destinationMarker by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val lineColor = MaterialTheme.colorScheme.outlineVariant

    Card(shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .onGloballyPositioned { column = it }
                    // The line joining the two markers, from below the first to above the second.
                    .drawBehind {
                        val space = column?.takeIf { it.isAttached } ?: return@drawBehind
                        val from = sourceMarker?.takeIf { it.isAttached }?.let { space.localBoundingBoxOf(it) } ?: return@drawBehind
                        val to = destinationMarker?.takeIf { it.isAttached }?.let { space.localBoundingBoxOf(it) } ?: return@drawBehind
                        val inset = 4.dp.toPx()
                        if (to.top - inset > from.bottom + inset) {
                            drawLine(
                                lineColor,
                                Offset(from.center.x, from.bottom + inset),
                                Offset(from.center.x, to.top - inset),
                                strokeWidth = 2.dp.toPx(),
                            )
                        }
                    }
            ) {
                LocationMenu(
                    label = stringResource(R.string.taxi_route_from),
                    isOrigin = true,
                    selection = source,
                    onSelectionChange = onSourceChange,
                    // The other end of the route can't be this end too.
                    locations = locations.filter { it.id != destination?.id },
                    onMarkerPlaced = { sourceMarker = it },
                )
                HorizontalDivider(Modifier.padding(start = 56.dp))
                LocationMenu(
                    label = stringResource(R.string.taxi_route_to),
                    isOrigin = false,
                    selection = destination,
                    onSelectionChange = onDestinationChange,
                    locations = locations.filter { it.id != source?.id },
                    onMarkerPlaced = { destinationMarker = it },
                )
            }
            FilledTonalIconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onSourceChange(destination)
                    onDestinationChange(source)
                },
                enabled = source != null || destination != null,
                shapes = IconButtonDefaults.shapes(),
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Icon(Icons.Rounded.SwapVert, contentDescription = stringResource(R.string.swap_route))
            }
        }

        HorizontalDivider()
        FavoriteRouteDropdown(
            favorites = favoriteRoutes,
            onSelect = onFavoriteSelect,
            onDelete = onFavoriteDelete,
            onAddCurrent = onFavoriteAdd,
            canAddCurrent = canAddFavorite
        )
    }
}

/**
 * One end of the route as a row: its marker, the chosen place or what the row is for, and a button
 * to clear the place once one is chosen. Tapping the row opens the places, popular ones first.
 */
@Composable
fun LocationMenu(
    label: String,
    isOrigin: Boolean,
    selection: TaxiLocation?,
    onSelectionChange: (TaxiLocation?) -> Unit,
    locations: List<TaxiLocation>,
    onMarkerPlaced: (LayoutCoordinates) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    val markerColor = if (selection != null) {
        if (isOrigin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    } else MaterialTheme.colorScheme.outline

    Box {
        ListItem(
            leadingContent = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .onGloballyPositioned(onMarkerPlaced)
                ) {
                    if (isOrigin) {
                        Icon(Icons.Rounded.NearMe, contentDescription = null, tint = markerColor, modifier = Modifier.size(20.dp))
                    } else {
                        Box(Modifier.size(11.dp).background(markerColor, CircleShape))
                    }
                }
            },
            headlineContent = {
                Text(
                    text = selection?.title?.localized() ?: label,
                    style = if (selection != null) MaterialTheme.typography.titleMedium
                    else MaterialTheme.typography.bodyLarge,
                    color = if (selection != null) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingContent = {
                if (selection != null) {
                    IconButton(
                        onClick = { onSelectionChange(null) },
                        shapes = IconButtonDefaults.shapes(),
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.taxi_route_clear, label),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                } else {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier
                .semantics { contentDescription = label }
                .clickable(onClickLabel = label) { expanded = true }
        )

        // Groups: the popular places, then every other place.
        ExpressiveMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            val (popular, others) = remember(locations) { TaxiLocation.popularFirst(locations) }
            val select: (TaxiLocation) -> Unit = {
                onSelectionChange(it)
                expanded = false
            }
            val groupCount = (if (popular.isNotEmpty()) 1 else 0) + (if (others.isNotEmpty()) 1 else 0)
            var groupIndex = 0

            if (popular.isNotEmpty()) {
                MenuGroup(index = groupIndex++, count = groupCount) {
                    popular.forEachIndexed { index, location ->
                        LocationMenuItem(
                            location,
                            isSelected = location.id == selection?.id,
                            shapes = MenuDefaults.itemShape(index, popular.size),
                            isPopular = true
                        ) { select(location) }
                    }
                }
            }
            if (others.isNotEmpty()) {
                MenuGroup(index = groupIndex, count = groupCount, maxHeight = 320.dp) {
                    others.forEachIndexed { index, location ->
                        LocationMenuItem(
                            location,
                            isSelected = location.id == selection?.id,
                            shapes = MenuDefaults.itemShape(index, others.size)
                        ) { select(location) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationCheckIcon() {
    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(MenuDefaults.LeadingIconSize))
}

@Composable
private fun LocationMenuItem(
    location: TaxiLocation,
    isSelected: Boolean,
    shapes: MenuItemShapes,
    isPopular: Boolean = false,
    onClick: () -> Unit,
) {
    // A selected place shows the check in place of its popular star.
    SelectableDropdownMenuItem(
        colors = selectableMenuItemColors(),
        selected = isSelected,
        text = {
            LocalizedText(text = location.title)
        },
        leadingIcon = if (isPopular) {
            {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = stringResource(R.string.popular_location)
                )
            }
        } else null,
        selectedLeadingIcon = { LocationCheckIcon() },
        onClick = onClick,
        shapes = shapes
    )
}

@Composable
@Preview(showBackground = true)
private fun LocationMenuItemPreview() {
    Theme {
        MenuGroup(index = 0, count = 1) {
            LocationMenuItem(TaxiLocation.mockList()[1], isSelected = true, MenuDefaults.itemShape(0, 2), isPopular = true) {}
            LocationMenuItem(TaxiLocation.mockList()[2], isSelected = false, MenuDefaults.itemShape(1, 2)) {}
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
            locations = TaxiLocation.mockList(),
            favoriteRoutes = listOf(
                TaxiFavoriteRoute(
                    id = "1",
                    from = TaxiLocation.mock(),
                    to = TaxiLocation.mockList()[1]
                )
            )
        )
    }
}
