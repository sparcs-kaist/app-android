package org.sparcs.soap.app.features.settings.timetable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.rememberTimetableThemeState
import org.sparcs.soap.app.theme.ui.rememberTimetableThemeStore

@Composable
fun TimetableThemeSettingsView(navController: NavController) {
    val store = rememberTimetableThemeStore()
    val state = rememberTimetableThemeState(store)
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
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
                onSave = store::saveAndSelect
            )
        } else {
            ThemeSettingsContent(
                state = state,
                onBack = { navController.popBackStack() },
                onSelect = store::select,
                onEdit = { editing = Json.encodeToString(it) },
                onDelete = { deleting = it },
                onShare = { sharing = Json.encodeToString(it) },
                onImport = { importing = true }
            )
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
