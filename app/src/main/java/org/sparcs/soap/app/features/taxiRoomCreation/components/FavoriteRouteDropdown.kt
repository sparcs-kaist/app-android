package org.sparcs.soap.app.features.taxiRoomCreation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiFavoriteRoute
import org.sparcs.soap.app.shared.extensions.LocalizedText
import org.sparcs.soap.app.shared.views.contentViews.ExpressiveMenu
import org.sparcs.soap.app.shared.views.contentViews.MenuGroup
import org.sparcs.soap.app.shared.views.contentViews.menuItemShape

/** A row that opens the saved routes, with an item to save the current one. */
@Composable
fun FavoriteRouteDropdown(
    favorites: List<TaxiFavoriteRoute>,
    onSelect: (TaxiFavoriteRoute) -> Unit,
    onDelete: (String) -> Unit,
    onAddCurrent: () -> Unit,
    canAddCurrent: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        ListItem(
            leadingContent = {
                Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            headlineContent = { Text(stringResource(R.string.favorite_routes)) },
            trailingContent = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable { expanded = true }
        )

        // Groups: the saved routes to pick from, then saving the current one.
        ExpressiveMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            val groupCount = if (canAddCurrent) 2 else 1
            MenuGroup(index = 0, count = groupCount, maxHeight = 320.dp) {
                if (favorites.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.no_favorite_routes)) },
                        onClick = { },
                        shape = menuItemShape(0, 1),
                        enabled = false
                    )
                } else {
                    favorites.forEachIndexed { index, favorite ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LocalizedText(text = favorite.from.title)
                                    Text(" → ")
                                    LocalizedText(text = favorite.to.title)
                                }
                            },
                            trailingContent = {
                                IconButton(onClick = { onDelete(favorite.id) }, shapes = IconButtonDefaults.shapes()) {
                                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete))
                                }
                            },
                            onClick = {
                                onSelect(favorite)
                                expanded = false
                            },
                            shape = menuItemShape(index, favorites.size)
                        )
                    }
                }
            }

            if (canAddCurrent) {
                MenuGroup(index = 1, count = groupCount) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.add_to_favorites)) },
                        leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        onClick = {
                            onAddCurrent()
                            expanded = false
                        },
                        shape = menuItemShape(0, 1)
                    )
                }
            }
        }
    }
}

