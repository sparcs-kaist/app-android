package org.sparcs.soap.app.features.taxiRoomCreation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiFavoriteRoute
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun TaxiFavoriteRoutes(
    source: TaxiLocation?,
    destination: TaxiLocation?,
    routes: List<TaxiFavoriteRoute>,
    onSelect: (TaxiFavoriteRoute) -> Unit,
    onSave: () -> Unit,
    onRemove: (TaxiFavoriteRoute) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val haptic = LocalHapticFeedback.current
    val canSave = source != null && destination != null && source.id != destination.id &&
        routes.none { it.from.id == source.id && it.to.id == destination.id }

    if (!canSave && routes.isEmpty()) return

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        if (canSave) {
            item(key = "save") {
                AssistChip(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        onSave()
                    },
                    label = { Text(stringResource(R.string.taxi_save_route)) },
                    leadingIcon = {
                        Icon(Icons.Outlined.StarOutline, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize))
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }
        items(routes, key = { it.id }) { route ->
            RouteChip(
                route = route,
                isChosen = route.from.id == source?.id && route.to.id == destination?.id,
                onSelect = {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onSelect(route)
                },
                onRemove = { onRemove(route) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RouteChip(
    route: TaxiFavoriteRoute,
    isChosen: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showsMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val removeLabel = stringResource(R.string.taxi_remove_from_favourites)
    val scheme = MaterialTheme.colorScheme

    Box(modifier) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = if (isChosen) scheme.secondaryContainer else Color.Transparent,
            contentColor = if (isChosen) scheme.onSecondaryContainer else scheme.onSurfaceVariant,
            border = if (isChosen) null else BorderStroke(1.dp, scheme.outlineVariant),
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .combinedClickable(
                    role = Role.Button,
                    onLongClickLabel = removeLabel,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showsMenu = true
                    },
                    onClick = onSelect
                )
                .semantics {
                    selected = isChosen
                    customActions = listOf(CustomAccessibilityAction(removeLabel) { onRemove(); true })
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .height(32.dp)
                    .padding(horizontal = 12.dp)
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(16.dp))
                Text(route.from.title.localized(), style = MaterialTheme.typography.labelLarge, maxLines = 1)
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = scheme.outline,
                    modifier = Modifier.size(14.dp)
                )
                Text(route.to.title.localized(), style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
        }

        DropdownMenu(expanded = showsMenu, onDismissRequest = { showsMenu = false }) {
            DropdownMenuItem(
                text = { Text(removeLabel, color = scheme.error) },
                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = scheme.error) },
                onClick = {
                    showsMenu = false
                    onRemove()
                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        val locations = TaxiLocation.mockList()
        val routes = listOf(
            TaxiFavoriteRoute("a", locations[0], locations[1]),
            TaxiFavoriteRoute("b", locations[2], locations[0]),
        )
        TaxiFavoriteRoutes(
            source = locations[0],
            destination = locations[3],
            routes = routes,
            onSelect = {},
            onSave = {},
            onRemove = {},
            contentPadding = PaddingValues(16.dp)
        )
    }
}
