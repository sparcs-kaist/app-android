package org.sparcs.soap.app.shared.views.contentViews

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.app.theme.ui.Theme

/**
 * A compose screen's editing actions as a Material 3 Expressive floating toolbar, kept at the
 * bottom start above the keyboard, with optional [trailing] status such as a character count at
 * the end of the same row. The caller lifts it above the keyboard (imePadding) and gesture bar.
 */
@Composable
fun ComposeToolbar(
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(FloatingToolbarDefaults.ScreenOffset)
    ) {
        // One step above the compose screens' background, where the default surfaceContainer
        // matches it in dark mode and the pill would vanish.
        HorizontalFloatingToolbar(
            expanded = true,
            colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            content = actions
        )
        if (trailing != null) {
            Spacer(Modifier.weight(1f))
            trailing()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ComposeToolbarPreview() {
    Theme {
        ComposeToolbar(
            trailing = {
                Text(
                    "12/280",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        ) {
            IconButton(onClick = {}, shapes = IconButtonDefaults.shapes()) {
                Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null)
            }
            IconButton(onClick = {}, shapes = IconButtonDefaults.shapes()) {
                Icon(Icons.Outlined.Tune, contentDescription = null)
            }
        }
    }
}
