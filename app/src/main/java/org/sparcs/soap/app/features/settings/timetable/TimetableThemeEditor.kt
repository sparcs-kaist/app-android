package org.sparcs.soap.app.features.settings.timetable

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TimetableThemeEditor(
    seed: String,
    onBack: () -> Unit,
    onSave: (TimetableTheme) -> Unit,
) {
    var draftJson by rememberSaveable(seed) { mutableStateOf(seed) }
    val draft = remember(draftJson) { Json.decodeFromString<TimetableTheme>(draftJson) }
    fun update(theme: TimetableTheme) {
        draftJson = Json.encodeToString(theme)
    }

    var expanded by rememberSaveable { mutableStateOf(false) }
    var target by rememberSaveable { mutableStateOf<String?>(null) }
    var discard by rememberSaveable { mutableStateOf(false) }
    val back = { if (draftJson != seed) discard = true else onBack() }
    BackHandler(onBack = back)

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = stringResource(R.string.theme_edit),
                onDismiss = back,
                isEditable = true,
                isDoneEnabled = draft.isValid,
                onClickDone = { onSave(draft) }
            )
        }
    ) { padding ->
        Box(Modifier
            .fillMaxSize()
            .padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
                    .imePadding(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { ThemePreview(draft) }
                item {
                    OutlinedTextField(
                        draft.name,
                        { update(draft.copy(name = it)) },
                        Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.theme_name)) },
                        singleLine = true
                    )
                }
                item {
                    ThemeColorRow(
                        stringResource(R.string.theme_text),
                        draft.textColorHex
                    ) { target = "text" }
                }
                item {
                    ThemeSectionTitle(stringResource(R.string.theme_palette))
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 4
                    ) {
                        draft.hexColors.forEachIndexed { index, hex ->
                            val colorLabel = stringResource(R.string.theme_color, index + 1)
                            FilledTonalButton(
                                onClick = { target = index.toString() },
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { contentDescription = colorLabel },
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                Box(
                                    Modifier
                                        .size(24.dp)
                                        .background(TimetableTheme.color(hex), CircleShape)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("${index + 1}")
                            }
                        }
                    }
                }
                item {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.theme_advanced)) },
                        trailingContent = {
                            Icon(
                                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null
                            )
                        },
                        modifier = Modifier.clickable(role = Role.Button) { expanded = !expanded })
                    AnimatedVisibility(expanded) {
                        Column {
                            OptionalThemeColor(
                                stringResource(R.string.theme_separator),
                                draft.separatorColorHex,
                                MaterialTheme.colorScheme.outlineVariant,
                                { update(draft.copy(separatorColorHex = it)) }) {
                                target = "separator"
                            }
                            OptionalThemeColor(
                                stringResource(R.string.theme_background),
                                draft.backgroundColorHex,
                                MaterialTheme.colorScheme.surface,
                                { update(draft.copy(backgroundColorHex = it)) }) {
                                target = "background"
                            }
                            OptionalThemeColor(
                                stringResource(R.string.theme_labels),
                                draft.gridLabelColorHex,
                                draft.gridLabelColor ?: MaterialTheme.colorScheme.onSurface,
                                { update(draft.copy(gridLabelColorHex = it)) }) {
                                target = "labels"
                            }
                        }
                    }
                }
            }
        }
    }
    target?.let { key ->
        val index = key.toIntOrNull()
        val hex = when (key) {
            "text" -> draft.textColorHex; "separator" -> draft.separatorColorHex; "background" -> draft.backgroundColorHex; "labels" -> draft.gridLabelColorHex; else -> draft.hexColors.getOrNull(
                index ?: -1
            )
        }
        if (hex != null) AdvancedColorPicker(
            hex,
            onDismiss = { target = null },
            onApply = { value ->
                update(
                    when (key) {
                        "text" -> draft.copy(textColorHex = value)
                        "separator" -> draft.copy(separatorColorHex = value)
                        "background" -> draft.copy(backgroundColorHex = value)
                        "labels" -> draft.copy(gridLabelColorHex = value)
                        else -> draft.copy(hexColors = draft.hexColors.mapIndexed { i, old -> if (i == index) value else old })
                    }
                ); target = null
            },
            onRemove = if (index != null && draft.hexColors.size > 1) ({
                update(draft.copy(hexColors = draft.hexColors.filterIndexed { i, _ -> i != index })); target =
                null
            }) else null
        )
    }
    if (discard) AlertDialog(
        onDismissRequest = { discard = false },
        title = { Text(stringResource(R.string.theme_discard_title)) },
        text = { Text(stringResource(R.string.theme_discard_message)) },
        containerColor = MaterialTheme.colorScheme.background,
        confirmButton = { TextButton(onClick = onBack) { Text(stringResource(R.string.theme_discard)) } },
        dismissButton = {
            TextButton(onClick = {
                discard = false
            }) { Text(stringResource(R.string.cancel)) }
        })
}

@Composable
private fun ThemeColorRow(label: String, hex: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = { Text("#$hex") },
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

@Preview(showBackground = true)
@Composable
private fun TimetableThemeEditorPreview() {
    MaterialTheme {
        TimetableThemeEditor(
            seed = Json.encodeToString(TimetableTheme.Default),
            onBack = {},
            onSave = {}
        )
    }
}
