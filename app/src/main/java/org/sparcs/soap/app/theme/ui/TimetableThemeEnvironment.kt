package org.sparcs.soap.app.theme.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.helpers.TimetableThemeStore

val LocalTimetableTheme = staticCompositionLocalOf { TimetableTheme.Default }

@Composable
fun rememberTimetableThemeStore(): TimetableThemeStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { TimetableThemeStore(context) }
}

@Composable
fun rememberTimetableThemeState(store: TimetableThemeStore): TimetableThemeStore.State {
    var state by remember(store) { mutableStateOf(store.state) }
    DisposableEffect(store) {
        val stop = store.observe { state = it }
        onDispose { stop() }
    }
    return state
}
