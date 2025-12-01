package com.sparcs.soap.Shared.Extensions


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize

@Composable
fun Color.asDiagonalGradientIfDark(
    startColor: Color,
    alpha: Float = 0.5f
): Brush {
    var size by remember { mutableStateOf(IntSize(0, 0)) }

    val brush = if (!isSystemInDarkTheme() && size.width > 0 && size.height > 0) {
        Brush.linearGradient(
            colors = listOf(startColor.copy(alpha = alpha), this.copy(alpha = alpha)),
            start = Offset(0f, 0f),
            end = Offset(size.width / 3f, size.height / 3f)
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