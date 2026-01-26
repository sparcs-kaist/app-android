package org.sparcs.soap.App.Shared.Extensions


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun Color.asDiagonalGradientIfDark(
    startColor: Color,
    alpha: Float = 0.5f
): Brush {
    val brush = if (!isSystemInDarkTheme()) {
        Brush.linearGradient(
            colors = listOf(startColor.copy(alpha = alpha), this.copy(alpha = alpha)),
            start = Offset(0f, 0f),
            end = Offset(15f, 3f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(this, this),
            start = Offset(0f, 0f),
            end = Offset(0f, 0f)
        )
    }
    return brush
}