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
import org.sparcs.soap.R
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
    var askingForCode by rememberSaveable { mutableStateOf(false) }

    ThemeSettingsContent(
        state = state,
        onBack = { navController.popBackStack() },
        onSelect = store::select,
        onEdit = { navController.navigateToThemeEditor(it) },
        onDelete = { deleting = it },
        onShare = { navController.navigateToThemeSharing(it) },
        onImport = { askingForCode = true }
    )

    if (askingForCode) {
        TimetableThemeCodeDialog(
            onDismiss = { askingForCode = false },
            onContinue = { code ->
                askingForCode = false
                navController.navigateToThemeImport(code)
            }
        )
    }

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
