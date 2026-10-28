package org.sparcs.soap.app.shared.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

/**
 * Runs [onPopped] once the current destination has been popped off the back stack, however
 * that happened (top bar button, system back or a predictive back gesture). Use it for clean-up
 * instead of a BackHandler, which would stop the system from animating the back gesture.
 */
@Composable
fun OnPoppedEffect(navController: NavController, onPopped: () -> Unit) {
    val entry = LocalLifecycleOwner.current as? NavBackStackEntry ?: return
    val currentOnPopped by rememberUpdatedState(onPopped)
    DisposableEffect(entry) {
        onDispose {
            val stillOnBackStack = runCatching {
                navController.getBackStackEntry(entry.destination.id) === entry
            }.getOrDefault(false)
            if (!stillOnBackStack) currentOnPopped()
        }
    }
}
