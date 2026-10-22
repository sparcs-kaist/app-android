package org.sparcs.soap.app.shared.sharing

import android.app.Activity
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.LifecycleStartEffect

@Composable
fun ScreenshotEffect(onScreenshot: () -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE || LocalInspectionMode.current) return
    val activity = LocalActivity.current ?: return
    val currentOnScreenshot by rememberUpdatedState(onScreenshot)
    LifecycleStartEffect(activity) {
        val callback = Activity.ScreenCaptureCallback { currentOnScreenshot() }
        activity.registerScreenCaptureCallback(activity.mainExecutor, callback)
        onStopOrDispose { activity.unregisterScreenCaptureCallback(callback) }
    }
}
