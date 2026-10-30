package org.sparcs.soap.app.features.fullscreenImage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.features.fullscreenImage.ImageAction
import org.sparcs.soap.app.theme.ui.Theme

@Composable
internal fun ViewerTopBar(
    title: String?,
    actionsEnabled: Boolean,
    onClose: () -> Unit,
    onAction: (ImageAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconColors = IconButtonDefaults.iconButtonColors(
        contentColor = Color.White,
        disabledContentColor = Color.White.copy(alpha = 0.38f)
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        IconButton(onClick = onClose, colors = iconColors) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close))
        }
        Text(
            text = title.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        ImageActionsMenu(enabled = actionsEnabled, iconColors = iconColors, onAction = onAction)
    }
}

@Composable
private fun ImageActionsMenu(
    enabled: Boolean,
    iconColors: IconButtonColors,
    onAction: (ImageAction) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }, enabled = enabled, colors = iconColors) {
            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more))
        }
        DropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            ImageAction.entries.forEach { action ->
                DropdownMenuItem(
                    text = { Text(stringResource(action.label)) },
                    leadingIcon = { Icon(action.icon, contentDescription = null) },
                    onClick = {
                        expanded = false
                        onAction(action)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun ViewerTopBarPreview() {
    Theme {
        ViewerTopBar(
            title = "2 of 5",
            actionsEnabled = true,
            onClose = {},
            onAction = {},
            modifier = Modifier.background(Color.Black)
        )
    }
}
