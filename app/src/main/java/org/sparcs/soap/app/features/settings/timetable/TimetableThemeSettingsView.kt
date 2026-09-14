package org.sparcs.soap.app.features.settings.timetable

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.theme.ui.rememberTimetableThemeState
import org.sparcs.soap.app.theme.ui.rememberTimetableThemeStore

@Composable
fun TimetableTheme.displayName(): String = if (!isBuiltIn) name else stringResource(when (id) {
    "builtin.legacy" -> R.string.theme_legacy
    "builtin.olive" -> R.string.theme_olive
    "builtin.cherryBlossom" -> R.string.theme_cherryblossom
    "builtin.spring" -> R.string.theme_spring
    "builtin.summer" -> R.string.theme_summer
    "builtin.autumn" -> R.string.theme_autumn
    "builtin.winter" -> R.string.theme_winter
    "builtin.ocean" -> R.string.theme_ocean
    "builtin.sunset" -> R.string.theme_sunset
    "builtin.monochrome" -> R.string.theme_monochrome
    else -> R.string.theme_default
})

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableThemeSettingsView(onBack: () -> Unit) {
    val store = rememberTimetableThemeStore()
    val state = rememberTimetableThemeState(store)
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    var deleting by rememberSaveable { mutableStateOf<String?>(null) }
    val seed = editing
    if (seed != null) {
        TimetableThemeEditor(seed, onBack = { editing = null }, onSave = { store.saveAndSelect(it); editing = null })
        return
    }
    val newName = stringResource(R.string.theme_my_name)
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.timetable_theme)) }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
        }) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = 680.dp).fillMaxSize().selectableGroup(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { ThemePreview(state.selected) }
                item { ThemeSectionTitle(stringResource(R.string.theme_collections)) }
                items(state.themes, key = { it.id }) { theme ->
                    if (!theme.isBuiltIn && theme == state.customThemes.firstOrNull()) ThemeSectionTitle(stringResource(R.string.theme_mine))
                    val copyName = stringResource(R.string.theme_copy, theme.displayName())
                    ThemeRow(theme, state.selectedID == theme.id,
                        onSelect = { store.select(theme.id) },
                        onEdit = { editing = Json.encodeToString(theme) },
                        onDuplicate = { editing = Json.encodeToString(theme.duplicate(copyName)) },
                        onDelete = { deleting = theme.id })
                }
                item {
                    if (state.customThemes.isEmpty()) ThemeSectionTitle(stringResource(R.string.theme_mine))
                    OutlinedButton(onClick = { editing = Json.encodeToString(state.selected.duplicate(newName)) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.theme_new))
                    }
                    Text(stringResource(R.string.theme_presets_note), Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
    state.customThemes.firstOrNull { it.id == deleting }?.let { theme ->
        AlertDialog(onDismissRequest = { deleting = null }, title = { Text(stringResource(R.string.theme_delete_title)) },
            text = { Text(stringResource(R.string.theme_delete_message, theme.name)) },
            confirmButton = { TextButton(onClick = { store.delete(theme.id); deleting = null }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.cancel)) } })
    }
}

@Composable
internal fun ThemeSectionTitle(title: String) {
    Text(title, Modifier.padding(top = 16.dp, bottom = 8.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun ThemeRow(theme: TimetableTheme, selected: Boolean, onSelect: () -> Unit, onEdit: () -> Unit, onDuplicate: () -> Unit, onDelete: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Surface(color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow, shape = MaterialTheme.shapes.medium) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).selectable(selected, role = Role.RadioButton, onClick = onSelect).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected, onClick = null)
                Column(Modifier.padding(start = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(theme.displayName(), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        theme.hexColors.take(7).forEach { Box(Modifier.size(16.dp).background(TimetableTheme.color(it), CircleShape)) }
                    }
                }
            }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, stringResource(R.string.theme_options, theme.displayName())) }
                DropdownMenu(menu, onDismissRequest = { menu = false }) {
                    if (!theme.isBuiltIn) DropdownMenuItem(text = { Text(stringResource(R.string.edit)) }, onClick = { menu = false; onEdit() })
                    DropdownMenuItem(text = { Text(stringResource(R.string.theme_duplicate)) }, onClick = { menu = false; onDuplicate() })
                    if (!theme.isBuiltIn) DropdownMenuItem(text = { Text(stringResource(R.string.delete)) }, onClick = { menu = false; onDelete() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TimetableThemeEditor(seed: String, onBack: () -> Unit, onSave: (TimetableTheme) -> Unit) {
    var draftJson by rememberSaveable(seed) { mutableStateOf(seed) }
    val draft = remember(draftJson) { Json.decodeFromString<TimetableTheme>(draftJson) }
    fun update(theme: TimetableTheme) { draftJson = Json.encodeToString(theme) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var target by rememberSaveable { mutableStateOf<String?>(null) }
    var discard by rememberSaveable { mutableStateOf(false) }
    val back = { if (draftJson != seed) discard = true else onBack() }
    BackHandler(onBack = back)
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.theme_edit)) }, navigationIcon = {
        IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
    }, actions = {
        TextButton(onClick = { onSave(draft) }, enabled = draft.isValid) { Text(stringResource(R.string.theme_save)) }
    }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = 680.dp).fillMaxSize().imePadding(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { ThemePreview(draft) }
                item { OutlinedTextField(draft.name, { update(draft.copy(name = it)) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.theme_name)) }, singleLine = true) }
                item { ThemeColorRow(stringResource(R.string.theme_text), draft.textColorHex) { target = "text" } }
                item {
                    ThemeSectionTitle(stringResource(R.string.theme_palette))
                    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), maxItemsInEachRow = 4) {
                        draft.hexColors.forEachIndexed { index, hex ->
                            val colorLabel = stringResource(R.string.theme_color, index + 1)
                            FilledTonalButton(onClick = { target = index.toString() }, modifier = Modifier.weight(1f).semantics { contentDescription = colorLabel }, contentPadding = PaddingValues(8.dp)) {
                                Box(Modifier.size(24.dp).background(TimetableTheme.color(hex), CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text("${index + 1}")
                            }
                        }
                    }
                }
                item {
                    ListItem(headlineContent = { Text(stringResource(R.string.theme_advanced)) }, trailingContent = { Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null) }, modifier = Modifier.clickable(role = Role.Button) { expanded = !expanded })
                    AnimatedVisibility(expanded) {
                        Column {
                            OptionalThemeColor(stringResource(R.string.theme_separator), draft.separatorColorHex, MaterialTheme.colorScheme.outlineVariant, { update(draft.copy(separatorColorHex = it)) }) { target = "separator" }
                            OptionalThemeColor(stringResource(R.string.theme_background), draft.backgroundColorHex, MaterialTheme.colorScheme.surface, { update(draft.copy(backgroundColorHex = it)) }) { target = "background" }
                            OptionalThemeColor(stringResource(R.string.theme_labels), draft.gridLabelColorHex, draft.gridLabelColor ?: MaterialTheme.colorScheme.onSurface, { update(draft.copy(gridLabelColorHex = it)) }) { target = "labels" }
                        }
                    }
                }
            }
        }
    }
    target?.let { key ->
        val index = key.toIntOrNull()
        val hex = when (key) { "text" -> draft.textColorHex; "separator" -> draft.separatorColorHex; "background" -> draft.backgroundColorHex; "labels" -> draft.gridLabelColorHex; else -> draft.hexColors.getOrNull(index ?: -1) }
        if (hex != null) ThemeColorDialog(hex, onDismiss = { target = null }, onApply = { value ->
            update(when (key) {
                "text" -> draft.copy(textColorHex = value)
                "separator" -> draft.copy(separatorColorHex = value)
                "background" -> draft.copy(backgroundColorHex = value)
                "labels" -> draft.copy(gridLabelColorHex = value)
                else -> draft.copy(hexColors = draft.hexColors.mapIndexed { i, old -> if (i == index) value else old })
            }); target = null
        }, onRemove = if (index != null && draft.hexColors.size > 1) ({ update(draft.copy(hexColors = draft.hexColors.filterIndexed { i, _ -> i != index })); target = null }) else null)
    }
    if (discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text(stringResource(R.string.theme_discard_title)) }, text = { Text(stringResource(R.string.theme_discard_message)) },
        confirmButton = { TextButton(onClick = onBack) { Text(stringResource(R.string.theme_discard)) } }, dismissButton = { TextButton(onClick = { discard = false }) { Text(stringResource(R.string.cancel)) } })
}

@Composable
private fun ThemeColorRow(label: String, hex: String, onClick: () -> Unit) {
    ListItem(headlineContent = { Text(label) }, supportingContent = { Text("#$hex") }, trailingContent = { Box(Modifier.size(32.dp).background(TimetableTheme.color(hex), CircleShape)) }, modifier = Modifier.clickable(onClick = onClick))
}

@Composable
private fun OptionalThemeColor(label: String, hex: String?, fallback: Color, onChange: (String?) -> Unit, onPick: () -> Unit) {
    ListItem(headlineContent = { Text(label) }, trailingContent = { Switch(hex != null, onCheckedChange = null) },
        modifier = Modifier.toggleable(value = hex != null, role = Role.Switch, onValueChange = { onChange(if (it) fallback.hex() else null) }))
    if (hex != null) ThemeColorRow(stringResource(R.string.theme_change_color), hex, onPick)
}
