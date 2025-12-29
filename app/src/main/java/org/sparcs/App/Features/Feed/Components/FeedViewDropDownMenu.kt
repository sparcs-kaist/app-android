package org.sparcs.App.Features.Feed.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.App.theme.ui.Theme
import org.sparcs.App.theme.ui.darkGray
import org.sparcs.App.theme.ui.lightGray0
import org.sparcs.R

@Composable
fun FeedViewDropDownMenu(
    onClickSettings: () -> Unit,
    onClickNotification: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { expanded = !expanded },
        colors = IconButtonDefaults.iconButtonColors(Color.Transparent)
    ) {
        Icon(
            painter = painterResource(R.drawable.more_horiz),
            contentDescription = "Menu",
            tint = MaterialTheme.colorScheme.darkGray,
            modifier = Modifier.size(30.dp)
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            DropDownItems(
                text = stringResource(R.string.notification),
                icon = R.drawable.baseline_notifications
            ) { onClickNotification() }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.lightGray0,
                modifier = Modifier.padding(4.dp)
            )

            DropDownItems(
                text = stringResource(R.string.settings),
                icon = R.drawable.round_settings
            ) { onClickSettings() }
        }
    }
}

@Composable
private fun DropDownItems(
    text: String,
    icon: Int,
    color: Color = MaterialTheme.colorScheme.onSurface,
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
        leadingIcon = {
            Icon(
                painter = painterResource(icon),
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
                FeedViewDropDownMenu({}, {})
            }
        }
    }
}