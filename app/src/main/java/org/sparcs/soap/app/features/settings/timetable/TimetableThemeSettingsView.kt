package org.sparcs.soap.app.features.settings.timetable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.rememberTimetableThemeState
import org.sparcs.soap.app.theme.ui.rememberTimetableThemeStore

@Composable
fun TimetableTheme.displayName(): String = if (!isBuiltIn) name else stringResource(
    when (id) {
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
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableThemeSettingsView(onBack: () -> Unit) {
    val store = rememberTimetableThemeStore()
    val state = rememberTimetableThemeState(store)
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    var deleting by rememberSaveable { mutableStateOf<String?>(null) }
    var sharing by rememberSaveable { mutableStateOf<String?>(null) }
    var importing by rememberSaveable { mutableStateOf(false) }

    if (sharing != null || importing) {
        TimetableThemeExchangeRoute(
            sharing = sharing?.let { Json.decodeFromString<TimetableTheme>(it) },
            onBack = { sharing = null; importing = false },
            onImport = { theme ->
                store.saveAndSelect(theme)
                importing = false
            }
        )
        return
    }

    AnimatedContent(
        targetState = editing,
        label = "ThemeSettingsTransition",
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 4 } + fadeOut()
            } else {
                slideInHorizontally { -it / 4 } + fadeIn() togetherWith slideOutHorizontally { it }
            }
        }
    ) { currentEditing ->
        if (currentEditing != null) {
            TimetableThemeEditor(
                currentEditing,
                onBack = { editing = null },
                onSave = store::saveAndSelect)
        } else {
            val newName = stringResource(R.string.theme_my_name)
            Scaffold(
                topBar = {
                    SettingsViewNavigationBar(
                        title = stringResource(R.string.timetable_theme),
                        onDismiss = onBack
                    )
                },
            ) { padding ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding), contentAlignment = Alignment.TopCenter
                ) {
                    LazyColumn(
                        Modifier
                            .widthIn(max = 680.dp)
                            .fillMaxSize()
                            .selectableGroup(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { ThemePreview(state.selected) }
                        item { ThemeSectionTitle(stringResource(R.string.theme_collections)) }
                        items(state.themes, key = { it.id }) { theme ->
                            if (!theme.isBuiltIn && theme == state.customThemes.firstOrNull()) ThemeSectionTitle(
                                stringResource(R.string.theme_mine)
                            )
                            val copyName = stringResource(R.string.theme_copy, theme.displayName())
                            val shareName = theme.displayName()
                            ThemeRow(
                                theme, state.selectedID == theme.id,
                                onSelect = { store.select(theme.id) },
                                onEdit = { editing = Json.encodeToString(theme) },
                                onDuplicate = { editing = Json.encodeToString(theme.duplicate(copyName)) },
                                onDelete = { deleting = theme.id },
                                onShare = { sharing = Json.encodeToString(theme.copy(name = shareName)) })
                        }
                        item {
                            if (state.customThemes.isEmpty()) ThemeSectionTitle(stringResource(R.string.theme_mine))
                            OutlinedButton(
                                onClick = {
                                    editing = Json.encodeToString(state.selected.duplicate(newName))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.theme_new))
                            }
                            OutlinedButton(
                                onClick = { importing = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.Download, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.theme_import))
                            }
                            Text(
                                stringResource(R.string.theme_presets_note),
                                Modifier.padding(top = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    state.customThemes.firstOrNull { it.id == deleting }?.let { theme ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.theme_delete_title)) },
            text = { Text(stringResource(R.string.theme_delete_message, theme.name)) },
            containerColor = MaterialTheme.colorScheme.background,
            confirmButton = {
                TextButton(onClick = {
                    store.delete(theme.id); deleting = null
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    deleting = null
                }) { Text(stringResource(R.string.cancel)) }
            })
    }
}

@Composable
internal fun ThemeSectionTitle(title: String) {
    Text(
        title,
        Modifier.padding(top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Preview(showBackground = true)
@Composable
private fun TimetableThemeSettingsViewPreview() {
    Theme {
        TimetableThemeSettingsView(onBack = {})
    }
}
