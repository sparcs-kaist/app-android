package org.sparcs.soap.App.Shared.Extensions

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshHapticHandler(
    state: PullToRefreshState,
    isRefreshing: Boolean
) {
    val context = LocalContext.current
    val vibrator = remember {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    }
    var hasVibrated by remember { mutableStateOf(false) }

    LaunchedEffect(state.distanceFraction) {
        if (state.distanceFraction >= 1f && !hasVibrated && !isRefreshing) {
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            }
            hasVibrated = true
        }
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) hasVibrated = false
    }
}

fun HapticFeedback.toggle(isOn: Boolean) {
    performHapticFeedback(if (isOn) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
}