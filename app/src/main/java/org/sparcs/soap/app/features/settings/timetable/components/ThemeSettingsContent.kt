package org.sparcs.soap.app.features.settings.timetable.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.helpers.TimetableThemeStore.State
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.grayBB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThemeSettingsContent(
    state: State,
    onBack: () -> Unit,
    onSelect: (String) -> Unit,
    onEdit: (TimetableTheme) -> Unit,
    onDelete: (String) -> Unit,
    onShare: (TimetableTheme) -> Unit,
    onImport: () -> Unit,
) {
    val newName = stringResource(R.string.theme_my_name)
    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = stringResource(R.string.timetable_theme),
                onDismiss = onBack
            )
        },
    ) { padding ->
        ThemeSettingsList(
            padding = padding,
            modifier = Modifier.selectableGroup(),
        ) {
            item(key = "preview") {
                Column(Modifier.padding(bottom = 24.dp)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.theme_preview),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    ThemePreview(state.selected)
                }
            }
            item(key = "my_themes") {
                ThemeSettingsSectionTitle(stringResource(R.string.theme_mine))
            }
            items(state.customThemes, key = { it.id }) { theme ->
                val copyName = stringResource(R.string.theme_copy, theme.displayName())
                ThemeRow(
                    theme, state.selectedID == theme.id,
                    onSelect = { onSelect(theme.id) },
                    onEdit = { onEdit(theme) },
                    onDuplicate = {
                        onEdit(theme.duplicate(copyName))
                    },
                    onDelete = { onDelete(theme.id) },
                    onShare = { onShare(theme) })
            }
            item(key = "new_theme") {
                ThemeSettingsAction(
                    text = stringResource(R.string.theme_new),
                    icon = Icons.Default.Add,
                    onClick = {
                        onEdit(state.selected.duplicate(newName))
                    }
                )
            }
            item(key = "import_theme") {
                ThemeSettingsAction(
                    text = stringResource(R.string.theme_import),
                    icon = Icons.Outlined.Download,
                    onClick = { onImport() }
                )
            }
            item(key = "collections") {
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
                ThemeSettingsSectionTitle(stringResource(R.string.theme_collections))
                Text(
                    stringResource(R.string.theme_presets_note),
                    Modifier.padding(start = 8.dp, end = 8.dp, bottom = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.grayBB
                )
            }
            items(TimetableTheme.builtIn, key = { it.id }) { theme ->
                val copyName = stringResource(R.string.theme_copy, theme.displayName())
                val shareName = theme.displayName()
                ThemeRow(
                    theme, state.selectedID == theme.id,
                    onSelect = { onSelect(theme.id) },
                    onEdit = {},
                    onDuplicate = {
                        onEdit(theme.duplicate(copyName))
                    },
                    onDelete = {},
                    onShare = {
                        onShare(theme.copy(name = shareName))
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 800, locale = "en")
@Preview(showBackground = true, widthDp = 420, heightDp = 800, locale = "ko")
@Composable
private fun ThemeSettingsContentPreview() {
    Theme {
        ThemeSettingsContent(
            state = State(emptyList(), TimetableTheme.Default.id),
            onBack = {}, onSelect = {}, onEdit = {}, onDelete = {}, onShare = {}, onImport = {}
        )
    }
}
