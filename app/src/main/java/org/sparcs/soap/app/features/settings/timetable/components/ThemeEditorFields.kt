package org.sparcs.soap.app.features.settings.timetable.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.theme.ui.Theme

@Composable
internal fun ThemeColorRow(label: String, hex: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = { Text(stringResource(R.string.theme_color_hex_value, hex)) },
        trailingContent = {
            Box(
                Modifier
                    .size(32.dp)
                    .background(TimetableTheme.color(hex), CircleShape)
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun OptionalThemeColor(
    label: String,
    hex: String?,
    fallback: Color,
    onChange: (String?) -> Unit,
    onPick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = { Switch(hex != null, onCheckedChange = null) },
        modifier = Modifier.toggleable(
            value = hex != null,
            role = Role.Switch,
            onValueChange = { onChange(if (it) fallback.hex() else null) })
    )
    if (hex != null) ThemeColorRow(stringResource(R.string.theme_change_color), hex, onPick)
}

@Composable
internal fun ThemeNameField(name: String, onNameChange: (String) -> Unit) {
    OutlinedTextField(
        name,
        onNameChange,
        Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.theme_name)) },
        isError = name.isBlank(),
        supportingText = {
            if (name.isBlank()) Text(stringResource(R.string.theme_name_required))
        },
        singleLine = true
    )
}

@Composable
internal fun ThemeAdvancedColors(
    draft: TimetableTheme,
    expanded: Boolean,
    onToggle: () -> Unit,
    update: (TimetableTheme) -> Unit,
    onPick: (String) -> Unit,
) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.theme_advanced)) },
        trailingContent = {
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null
            )
        },
        modifier = Modifier.clickable(role = Role.Button, onClick = onToggle)
    )
    AnimatedVisibility(expanded) {
        Column {
            OptionalThemeColor(
                stringResource(R.string.theme_separator),
                draft.separatorColorHex,
                MaterialTheme.colorScheme.outlineVariant,
                { update(draft.copy(separatorColorHex = it)) }) {
                onPick("separator")
            }
            OptionalThemeColor(
                stringResource(R.string.theme_background),
                draft.backgroundColorHex,
                MaterialTheme.colorScheme.surface,
                { update(draft.copy(backgroundColorHex = it)) }) {
                onPick("background")
            }
            OptionalThemeColor(
                stringResource(R.string.theme_labels),
                draft.gridLabelColorHex,
                draft.gridLabelColor ?: MaterialTheme.colorScheme.onSurface,
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
            ThemeNameField(stringResource(R.string.theme_my_name), {})
            ThemeColorRow(stringResource(R.string.theme_text), "FFFFFF", {})
            ThemeAdvancedColors(TimetableTheme.Default, true, {}, {}, {})
        }
    }
}
