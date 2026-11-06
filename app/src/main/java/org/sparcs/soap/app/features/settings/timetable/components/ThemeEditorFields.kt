package org.sparcs.soap.app.features.settings.timetable.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.features.settings.components.SettingsFieldItem
import org.sparcs.soap.app.features.settings.components.SettingsGroup
import org.sparcs.soap.app.features.settings.components.SettingsGroupScope
import org.sparcs.soap.app.features.settings.components.SettingsNavigationItem
import org.sparcs.soap.app.features.settings.components.SettingsSwitchItem
import org.sparcs.soap.app.features.settings.components.settingsTextFieldColors
import org.sparcs.soap.app.theme.ui.Theme

/** A color the user can change: tapping opens the picker; the swatch shows the current color. */
@Composable
internal fun ThemeColorRow(label: String, hex: String, onClick: () -> Unit) {
    SettingsNavigationItem(
        text = label,
        supportingText = stringResource(R.string.theme_color_hex_value, hex),
        trailingContent = {
            Box(
                Modifier
                    .size(32.dp)
                    .background(TimetableTheme.color(hex), CircleShape)
            )
        },
        onClick = onClick
    )
}

/** A switch for an optional color and, while it is on, the row that changes it. */
private fun SettingsGroupScope.optionalThemeColor(
    label: @Composable () -> String,
    hex: String?,
    fallback: @Composable () -> Color,
    onChange: (String?) -> Unit,
    onPick: () -> Unit,
) {
    row {
        val fallbackColor = fallback()
        SettingsSwitchItem(
            text = label(),
            checked = hex != null,
            onCheckedChange = { onChange(if (it) fallbackColor.hex() else null) }
        )
    }
    if (hex != null) row { ThemeColorRow(stringResource(R.string.theme_change_color), hex, onPick) }
}

/** The theme's name, as a field inside a settings card. */
@Composable
internal fun ThemeNameField(
    name: String,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        name,
        onNameChange,
        modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.theme_name)) },
        isError = name.isBlank(),
        supportingText = if (name.isBlank()) {
            { Text(stringResource(R.string.theme_name_required)) }
        } else null,
        singleLine = true,
        colors = settingsTextFieldColors()
    )
}

/** "Advanced" as one card: the row that expands it, then the optional colors while expanded. */
@Composable
internal fun ThemeAdvancedColors(
    draft: TimetableTheme,
    expanded: Boolean,
    onToggle: () -> Unit,
    update: (TimetableTheme) -> Unit,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsGroup(modifier.animateContentSize()) {
        row {
            SettingsNavigationItem(
                text = stringResource(R.string.theme_advanced),
                trailingContent = {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null
                    )
                },
                onClick = onToggle
            )
        }
        if (expanded) {
            optionalThemeColor(
                { stringResource(R.string.theme_separator) },
                draft.separatorColorHex,
                { MaterialTheme.colorScheme.outlineVariant },
                { update(draft.copy(separatorColorHex = it)) }) {
                onPick("separator")
            }
            optionalThemeColor(
                { stringResource(R.string.theme_background) },
                draft.backgroundColorHex,
                { MaterialTheme.colorScheme.surface },
                { update(draft.copy(backgroundColorHex = it)) }) {
                onPick("background")
            }
            optionalThemeColor(
                { stringResource(R.string.theme_labels) },
                draft.gridLabelColorHex,
                { draft.gridLabelColor ?: MaterialTheme.colorScheme.onSurface },
                { update(draft.copy(gridLabelColorHex = it)) }) {
                onPick("labels")
            }
        }
    }
}

@Preview(showBackground = true, locale = "en")
@Preview(showBackground = true, locale = "ko")
@Composable
private fun ThemeEditorFieldsPreview() {
    Theme {
        Column {
            SettingsGroup {
                row { SettingsFieldItem { ThemeNameField(stringResource(R.string.theme_my_name), {}) } }
                row { ThemeColorRow(stringResource(R.string.theme_text), "FFFFFF", {}) }
            }
            ThemeAdvancedColors(TimetableTheme.Default, true, {}, {}, {}, Modifier.padding(top = 16.dp))
        }
    }
}
