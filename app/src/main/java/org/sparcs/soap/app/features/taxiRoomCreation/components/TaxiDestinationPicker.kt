package org.sparcs.soap.app.features.taxiRoomCreation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.WhereToVote
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import org.sparcs.soap.app.theme.ui.Theme

/**
 * The route as a card, like a maps app's directions box: a row each for the meeting point and the
 * destination, a swap button beside them, and the favourite routes below.
 */
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

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                LocationMenu(
                    label = stringResource(R.string.taxi_route_from),
                    icon = Icons.Filled.NearMe,
                    selection = source,
                    onSelectionChange = onSourceChange,
                    locations = locations
                )
                HorizontalDivider(Modifier.padding(start = 56.dp))
                LocationMenu(
                    label = stringResource(R.string.taxi_route_to),
                    icon = Icons.Filled.WhereToVote,
                    selection = destination,
                    onSelectionChange = onDestinationChange,
                    locations = locations
                )
            }
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onSourceChange(destination)
                    onDestinationChange(source)
                },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Icon(Icons.Filled.SwapVert, contentDescription = stringResource(R.string.swap_route))
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

/** One end of the route as a row; tapping it opens the places, popular ones first. */
@Composable
fun LocationMenu(
    label: String,
    icon: ImageVector,
    selection: TaxiLocation?,
    onSelectionChange: (TaxiLocation?) -> Unit,
    locations: List<TaxiLocation>
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        ListItem(
            leadingContent = { Icon(icon, contentDescription = null) },
            overlineContent = { Text(label) },
            headlineContent = {
                Text(
                    text = selection?.title?.localized() ?: stringResource(R.string.taxi_route_choose),
                    color = if (selection != null) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingContent = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable { expanded = true }
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.taxi_route_none)) },
                onClick = {
                    onSelectionChange(null)
                    expanded = false
                },
                trailingIcon = if (selection == null) {
                    { Icon(Icons.Filled.Check, contentDescription = null) }
                } else null
            )

            val (popular, others) = remember(locations) { TaxiLocation.popularFirst(locations) }
            val select: (TaxiLocation) -> Unit = {
                onSelectionChange(it)
                expanded = false
            }
            if (popular.isNotEmpty() || others.isNotEmpty()) HorizontalDivider()
            popular.forEach { location ->
                LocationMenuItem(location, isSelected = location.id == selection?.id, isPopular = true) { select(location) }
            }
            if (popular.isNotEmpty() && others.isNotEmpty()) HorizontalDivider()
            others.forEach { location ->
                LocationMenuItem(location, isSelected = location.id == selection?.id) { select(location) }
            }
        }
    }
}

@Composable
private fun LocationMenuItem(
    location: TaxiLocation,
    isSelected: Boolean,
    isPopular: Boolean = false,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
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
        trailingIcon = if (isSelected) {
            { Icon(Icons.Filled.Check, contentDescription = null) }
        } else null,
        onClick = onClick
    )
}

@Composable
@Preview(showBackground = true)
private fun LocationMenuItemPreview() {
    Theme {
        Column {
            LocationMenuItem(TaxiLocation.mockList()[1], isSelected = true, isPopular = true) {}
            LocationMenuItem(TaxiLocation.mockList()[2], isSelected = false) {}
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
