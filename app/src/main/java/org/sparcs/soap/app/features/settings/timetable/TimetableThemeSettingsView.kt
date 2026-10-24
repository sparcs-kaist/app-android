package org.sparcs.soap.app.features.settings.timetable

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.features.settings.timetable.components.ThemeSettingsContent
import org.sparcs.soap.app.shared.tips.LocalTipStore
import org.sparcs.soap.app.shared.tips.TipEvent
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.rememberTimetableThemeState
import org.sparcs.soap.app.theme.ui.rememberTimetableThemeStore

@Composable
fun TimetableThemeSettingsView(navController: NavController) {
    val store = rememberTimetableThemeStore()
    val state = rememberTimetableThemeState(store)
    val tipStore = LocalTipStore.current
    LaunchedEffect(Unit) { tipStore?.donate(TipEvent.ThemeSettingsOpened) }
    var deleting by rememberSaveable { mutableStateOf<String?>(null) }
    var sharing by rememberSaveable { mutableStateOf<String?>(null) }
    var importing by rememberSaveable { mutableStateOf(false) }

    if (sharing != null || importing) {
        TimetableThemeExchangeRoute(
            sharing = sharing?.let { Json.decodeFromString<TimetableTheme>(it) },
            onBack = {
                sharing = null
                importing = false
            },
            onImport = { theme ->
                store.saveAndSelect(theme)
                importing = false
            },
            navController = navController
        )
        return
    }

    ThemeSettingsContent(
        state = state,
        onBack = { navController.popBackStack() },
        onSelect = store::select,
        onEdit = { navController.navigateToThemeEditor(it) },
        onDelete = { deleting = it },
        onShare = { sharing = Json.encodeToString(it) },
        onImport = { importing = true }
    )

    state.customThemes.firstOrNull { it.id == deleting }?.let { theme ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.theme_delete_title)) },
            text = { Text(stringResource(R.string.theme_delete_message, theme.name)) },
            confirmButton = {
                TextButton(onClick = {
                    store.delete(theme.id)
                    deleting = null
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    deleting = null
                }) { Text(stringResource(R.string.cancel)) }
            })
    }
}

@Preview(showBackground = true)
@Composable
private fun TimetableThemeSettingsViewPreview() {
    Theme {
        TimetableThemeSettingsView(navController = rememberNavController())
    }
}
