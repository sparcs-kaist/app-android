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
import androidx.compose.ui.res.stringResource
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiFavoriteRoute
import org.sparcs.soap.app.shared.extensions.LocalizedText

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

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (favorites.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.no_favorite_routes)) },
                    onClick = { },
                    enabled = false
                )
            } else {
                favorites.forEach { favorite ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LocalizedText(text = favorite.from.title)
                                Text(" → ")
                                LocalizedText(text = favorite.to.title)
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = { onDelete(favorite.id) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete))
                            }
                        },
                        onClick = {
                            onSelect(favorite)
                            expanded = false
                        }
                    )
                }
            }

            if (canAddCurrent) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.add_to_favorites)) },
                    leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    onClick = {
                        onAddCurrent()
                        expanded = false
                    }
                )
            }
        }
    }
}

