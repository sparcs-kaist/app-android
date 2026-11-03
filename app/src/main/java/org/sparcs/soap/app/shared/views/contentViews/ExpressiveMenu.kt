package org.sparcs.soap.app.shared.views.contentViews

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.MenuAnchorPosition
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset

/**
 * Material 3 Expressive menu: a popup holding one or more [MenuGroup]s, each a rounded segment of
 * the menu. Items inside a group take [menuItemShape] so the first and last follow its corners.
 */
@Composable
fun ExpressiveMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    anchorPosition: MenuAnchorPosition = MenuAnchorPosition.Below,
    offset: DpOffset = DpOffset.Zero,
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenuPopup(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        popupPositionProvider = MenuDefaults.rememberDropdownMenuPopupPositionProvider(anchorPosition, offset),
        content = content,
    )
}

/**
 * The group at [index] of the menu's [count] groups, spaced from the next one. A finite [maxHeight]
 * makes a long group scroll on its own, since the popup itself does not scroll.
 */
@Composable
fun MenuGroup(
    index: Int,
    count: Int,
    maxHeight: Dp = Dp.Unspecified,
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenuGroup(shapes = MenuDefaults.groupShape(index, count)) {
        if (maxHeight == Dp.Unspecified) {
            content()
        } else {
            Column(
                modifier = Modifier
                    .heightIn(max = maxHeight)
                    .verticalScroll(rememberScrollState()),
                content = content,
            )
        }
    }
    if (index != count - 1) {
        Spacer(Modifier.height(MenuDefaults.GroupSpacing))
    }
}

/** Shape of the item at [index] of the [count] items in its group. */
@Composable
fun menuItemShape(index: Int, count: Int): Shape = MenuDefaults.itemShape(index, count).shape
