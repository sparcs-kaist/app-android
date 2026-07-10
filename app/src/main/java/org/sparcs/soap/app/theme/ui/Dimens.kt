package org.sparcs.soap.app.theme.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ScreenPadding {
    val horizontalPortrait = 16.dp
    val horizontalLandscape = 24.dp
}

/**
 * Horizontal padding for screen-level content, responsive to orientation.
 * Portrait: 16.dp, Landscape: 24.dp.
 */
@Composable
fun screenHorizontalPadding(): Dp {
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    return if (isLandscape) ScreenPadding.horizontalLandscape else ScreenPadding.horizontalPortrait
}
