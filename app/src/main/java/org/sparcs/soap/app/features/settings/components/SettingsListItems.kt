package org.sparcs.soap.app.features.settings.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

// Building blocks for settings screens. Rows are Material ListItems on a surface-coloured
// screen; free-form content such as text fields should be inset by 16dp to line up with them.

/** Section title in the style of Android's own settings, with an optional leading [icon]; [trailing] sits after the text. */
@Composable
fun SettingsSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(18.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )
        trailing?.invoke()
    }
}

/** Row that opens another screen or performs an action. */
@Composable
fun SettingsNavigationItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    val disabled = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ListItem(
        headlineContent = { Text(text) },
        supportingContent = supportingText?.let { { Text(it) } },
        leadingContent = leadingContent,
        colors = if (enabled) ListItemDefaults.colors() else ListItemDefaults.colors(
            headlineColor = disabled,
            supportingColor = disabled,
            leadingIconColor = disabled
        ),
        modifier = modifier.clickable(enabled = enabled, onClick = onClick)
    )
}

/** Row with a trailing switch; the whole row toggles it. */
@Composable
fun SettingsSwitchItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    headlineTrailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            if (headlineTrailing == null) {
                Text(text)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text)
                    headlineTrailing()
                }
            }
        },
        supportingContent = supportingText?.let { { Text(it) } },
        leadingContent = leadingContent,
        trailingContent = { Switch(checked = checked, onCheckedChange = null, enabled = enabled) },
        modifier = modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Switch,
            onValueChange = onCheckedChange
        )
    )
}

/** A service's brand mark at icon size, keeping its own colours. */
@Composable
fun ServiceLogo(@DrawableRes logo: Int) {
    Icon(
        painterResource(logo),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(24.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        Column {
            SettingsSectionHeader("Section")
            SettingsNavigationItem(
                text = "Notifications",
                onClick = {},
                leadingContent = { Icon(Icons.Outlined.Notifications, null) }
            )
            SettingsNavigationItem(
                text = "Taxi",
                onClick = {},
                leadingContent = { ServiceLogo(R.drawable.taxi_logo) }
            )
            SettingsSwitchItem(
                text = "Automatic",
                checked = true,
                onCheckedChange = {},
                supportingText = "Explains what the switch does."
            )
        }
    }
}
