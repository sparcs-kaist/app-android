package org.sparcs.soap.app.features.settings.timetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
internal fun ThemeGeneratorLifecycleEffect(generator: TimetableThemeGeneratorViewModel) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val inspecting = LocalInspectionMode.current
    DisposableEffect(generator, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> if (!inspecting) generator.refreshAvailability()
                Lifecycle.Event.ON_STOP -> generator.cancel()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            generator.reset()
        }
    }
}
