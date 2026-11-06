package org.sparcs.soap.app.features.settings.timetable.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.helpers.TimetableThemeStore.State
import org.sparcs.soap.app.features.settings.components.SettingsGroup
import org.sparcs.soap.app.features.settings.components.SettingsNavigationItem
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
import org.sparcs.soap.app.features.settings.components.SettingsSegmentRow
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.shared.tips.TipView
import org.sparcs.soap.app.theme.ui.Theme

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
    val topBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
            .analyticsScreen("TimetableThemeSettings"),
        topBar = {
            SettingsViewNavigationBar(
                title = stringResource(R.string.timetable_theme),
                onDismiss = onBack,
                containerColor = MaterialTheme.colorScheme.surface,
                scrollBehavior = topBarScrollBehavior,
                flexible = true,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        // Sections are segmented cards like the other settings pages; their rows are lazy items,
        // so each row is a SettingsSegmentRow that knows its place in the card.
        ThemeSettingsList(
            padding = padding,
            modifier = Modifier.selectableGroup(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item(key = "preview") {
                Column {
                    SettingsSectionHeader(stringResource(R.string.theme_preview))
                    ThemePreview(state.selected, Modifier.padding(horizontal = 16.dp))
                }
            }
            item(key = "theme_actions_tip") {
                TipView(BuddyTip.ThemeActions, modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp))
            }
            item(key = "my_themes") {
                SettingsSectionHeader(stringResource(R.string.theme_mine))
            }
            itemsIndexed(state.customThemes, key = { _, theme -> theme.id }) { index, theme ->
                val copyName = stringResource(R.string.theme_copy, theme.displayName())
                SettingsSegmentRow(index, state.customThemes.size, Modifier.animateItem()) {
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
            }
            item(key = "theme_actions") {
                SettingsGroup(
                    Modifier.padding(top = if (state.customThemes.isEmpty()) 0.dp else 16.dp)
                ) {
                    row {
                        SettingsNavigationItem(
                            text = stringResource(R.string.theme_new),
                            onClick = { onEdit(state.selected.duplicate(newName)) },
                            leadingContent = { Icon(Icons.Default.Add, contentDescription = null) }
                        )
                    }
                    row {
                        SettingsNavigationItem(
                            text = stringResource(R.string.theme_import),
                            onClick = onImport,
                            leadingContent = { Icon(Icons.Outlined.Download, contentDescription = null) }
                        )
                    }
                }
            }
            item(key = "collections") {
                Column {
                    SettingsSectionHeader(stringResource(R.string.theme_collections))
                    Text(
                        stringResource(R.string.theme_presets_note),
                        Modifier.padding(start = 32.dp, end = 32.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            itemsIndexed(TimetableTheme.builtIn, key = { _, theme -> theme.id }) { index, theme ->
                val copyName = stringResource(R.string.theme_copy, theme.displayName())
                val shareName = theme.displayName()
                SettingsSegmentRow(index, TimetableTheme.builtIn.size) {
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
