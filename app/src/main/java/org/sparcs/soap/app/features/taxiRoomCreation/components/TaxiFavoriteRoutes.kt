package org.sparcs.soap.app.features.taxiRoomCreation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiFavoriteRoute
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.views.contentViews.ExpressiveMenu
import org.sparcs.soap.app.shared.views.contentViews.MenuGroup
import org.sparcs.soap.app.shared.views.contentViews.menuItemShape
import org.sparcs.soap.app.theme.ui.Theme

/**
 * Your favourite routes as a row of chips, one tap from filling the route picker. While the picker
 * holds a route you haven't saved, a chip to save it leads the row; long-pressing a route offers to
 * remove it. Shows nothing until there is a route to offer or one to save.
 */
@Composable
fun TaxiFavoriteRoutes(
    favorites: List<TaxiFavoriteRoute>,
    source: TaxiLocation?,
    destination: TaxiLocation?,
    canSaveCurrent: Boolean,
    onSelect: (TaxiFavoriteRoute) -> Unit,
    onDelete: (String) -> Unit,
    onSaveCurrent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!canSaveCurrent && favorites.isEmpty()) return
    val haptic = LocalHapticFeedback.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .animateContentSize(MaterialTheme.motionScheme.fastSpatialSpec()),
    ) {
        if (canSaveCurrent) {
            RouteChip(
                icon = Icons.Rounded.StarOutline,
                container = MaterialTheme.colorScheme.secondaryContainer,
                content = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onSaveCurrent()
                },
            ) {
                Text(stringResource(R.string.taxi_save_route), style = MaterialTheme.typography.labelLarge)
            }
        }
        favorites.forEach { route ->
            FavoriteRouteChip(
                route = route,
                isChosen = route.from.id == source?.id && route.to.id == destination?.id,
                onSelect = {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onSelect(route)
                },
                onDelete = { onDelete(route.id) },
            )
        }
    }
}

/** A saved route, filled in when it is the route chosen; a long press offers to remove it. */
@Composable
private fun FavoriteRouteChip(
    route: TaxiFavoriteRoute,
    isChosen: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
) {
    var showsMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val from = route.from.title.localized()
    val to = route.to.title.localized()
    val removeLabel = stringResource(R.string.taxi_remove_favorite_route)

    Box {
        RouteChip(
            icon = Icons.Rounded.Star,
            iconTint = if (isChosen) null else MaterialTheme.colorScheme.primary,
            container = if (isChosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            content = if (isChosen) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            onClick = onSelect,
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showsMenu = true
            },
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = "$from → $to"
                selected = isChosen
                role = Role.Button
                customActions = listOf(CustomAccessibilityAction(removeLabel) { onDelete(); true })
            },
        ) {
            Text(from, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Text(to, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }

        ExpressiveMenu(expanded = showsMenu, onDismissRequest = { showsMenu = false }) {
            MenuGroup(index = 0, count = 1) {
                DropdownMenuItem(
                    text = { Text(removeLabel, color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    },
                    onClick = {
                        showsMenu = false
                        onDelete()
                    },
                    shape = menuItemShape(0, 1),
                )
            }
        }
    }
}

/** A chip with a leading icon: tonal, rounded, and tappable — and long-pressable when asked. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RouteChip(
    icon: ImageVector,
    container: Color,
    content: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color? = null,
    onLongClick: (() -> Unit)? = null,
    label: @Composable () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = content,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .heightIn(min = 32.dp)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Icon(icon, contentDescription = null, tint = iconTint ?: content, modifier = Modifier.size(18.dp))
            label()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    val locations = TaxiLocation.mockList()
    Theme {
        TaxiFavoriteRoutes(
            favorites = listOf(
                TaxiFavoriteRoute(id = "1", from = locations[0], to = locations[1]),
                TaxiFavoriteRoute(id = "2", from = locations[1], to = locations[2]),
            ),
            source = locations[0],
            destination = locations[1],
            canSaveCurrent = true,
            onSelect = {},
            onDelete = {},
            onSaveCurrent = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
