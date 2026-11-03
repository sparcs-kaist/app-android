package org.sparcs.soap.app.features.feed.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.shared.views.contentViews.ExpressiveMenu
import org.sparcs.soap.app.shared.views.contentViews.MenuGroup
import org.sparcs.soap.app.shared.views.contentViews.menuItemShape
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun FeedViewDropDownMenu(
    onClickSettings: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = !expanded }, shapes = IconButtonDefaults.shapes()) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.more)
        )
    }

    ExpressiveMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        MenuGroup(index = 0, count = 1) {
//            DropDownItems(
//                text = stringResource(R.string.notification),
//                icon = Icons.Rounded.Notifications
//            ) { onClickNotification() }
//
//            HorizontalDivider() TODO: 알림 기능

            DropDownItems(
                text = stringResource(R.string.settings),
                icon = Icons.Rounded.Settings
            ) { onClickSettings() }
        }
    }
}

@Composable
private fun DropDownItems(
    text: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                color = color
            )
        },
        onClick = { onClick() },
        shape = menuItemShape(0, 1),
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color
            )
        }
    )
}

@Composable
@Preview
private fun Preview() {
    Theme {
        Box(Modifier.fillMaxSize()) {
            Button(
                onClick = {}
            ) {
                FeedViewDropDownMenu {}
            }
        }
    }
}