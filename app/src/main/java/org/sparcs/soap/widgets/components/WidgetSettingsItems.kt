@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package org.sparcs.soap.widgets.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.features.settings.components.SettingsFieldItem
import org.sparcs.soap.app.features.settings.components.SettingsGroup
import org.sparcs.soap.app.features.settings.components.SettingsNavigationItem
import org.sparcs.soap.app.theme.ui.Theme

// Rows for the widgets' setup screens, built on the app's settings groups so the screens read like
// the app's own settings: segmented rows in a rounded card, choices in a radio dialog.

/** The theme mode a widget is drawn in: "System", "Light" or "Dark", as the widgets store it. */
@Composable
fun WidgetThemeItem(selectedTheme: String, onThemeSelected: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val options = listOf(
        "System" to stringResource(R.string.widget_system_default),
        "Light" to stringResource(R.string.widget_white_mode),
        "Dark" to stringResource(R.string.widget_dark_mode),
    )

    SettingsNavigationItem(
        text = stringResource(R.string.theme_mode),
        supportingText = (options.firstOrNull { it.first == selectedTheme } ?: options.first()).second,
        leadingContent = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
        onClick = { showDialog = true },
    )

    if (showDialog) {
        WidgetChoiceDialog(title = stringResource(R.string.theme_mode), onDismiss = { showDialog = false }) {
            options.forEach { (key, label) ->
                item(key = key) {
                    WidgetChoiceRow(
                        selected = selectedTheme == key,
                        onClick = { onThemeSelected(key); showDialog = false },
                    ) { Text(label) }
                }
            }
        }
    }
}

/** How opaque the widget's background is, from 0 to 100%, as a slider row. */
@Composable
fun WidgetTransparencyItem(transparency: Float, onTransparencyChange: (Float) -> Unit) {
    SettingsFieldItem {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.transparency),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${(transparency * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val state = remember { SliderState(value = transparency) }
            // The saved value loads after the first frame, so follow it when it changes.
            LaunchedEffect(transparency) { if (state.value != transparency) state.value = transparency }
            Slider(state = state, onValueChange = { state.value = it; onTransparencyChange(it) })
        }
    }
}

/**
 * A dialog of choices, like the app's theme picker: [content] adds [WidgetChoiceRow]s, and a
 * choice closes it, as does Cancel.
 */
@Composable
fun WidgetChoiceDialog(title: String, onDismiss: () -> Unit, content: LazyListScope.() -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { LazyColumn(Modifier.selectableGroup(), content = content) },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

/** One choice in a [WidgetChoiceDialog]; the whole row selects it, and the radio shows which is chosen. */
@Composable
fun WidgetChoiceRow(selected: Boolean, onClick: () -> Unit, content: @Composable RowScope.() -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    ) {
        RadioButton(selected = selected, onClick = null)
        content()
    }
}

/** The setup screen's save button, full width under the settings. */
@Composable
fun WidgetSaveButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shapes = ButtonDefaults.shapes(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .heightIn(min = ButtonDefaults.MediumContainerHeight),
        contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
    ) {
        Text(stringResource(R.string.save_configuration), style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight))
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        Surface {
            Column {
                SettingsGroup {
                    row { WidgetThemeItem("Dark") {} }
                    row { WidgetTransparencyItem(0.8f) {} }
                }
                WidgetSaveButton {}
            }
        }
    }
}
