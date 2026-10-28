package org.sparcs.soap.app.features.settings.timetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Ties [generator] to the current lifecycle: availability is refreshed on resume. A screen that
 * runs generation passes [cancelOnStop] so it stops in the background; [resetOnDispose] clears
 * the generator when the screen leaves composition.
 */
@Composable
internal fun ThemeGeneratorLifecycleEffect(
    generator: TimetableThemeGeneratorViewModel,
    cancelOnStop: Boolean = true,
    resetOnDispose: Boolean = true,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val inspecting = LocalInspectionMode.current
    DisposableEffect(generator, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> if (!inspecting) generator.refreshAvailability()
                Lifecycle.Event.ON_STOP -> if (cancelOnStop) generator.cancel()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            if (resetOnDispose) generator.reset()
        }
    }
}
