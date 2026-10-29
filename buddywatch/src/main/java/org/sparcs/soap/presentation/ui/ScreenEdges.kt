package org.sparcs.soap.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val ROUND_HORIZONTAL_INSET = 0.1f
private const val ROUND_BOTTOM_FADE = 0.2f
private val SquareHorizontalInset = 14.dp

@Composable
fun edgeHorizontalPadding(): Dp {
    val configuration = LocalConfiguration.current
    return if (configuration.isScreenRound) configuration.screenWidthDp.dp * ROUND_HORIZONTAL_INSET else SquareHorizontalInset
}

@Composable
fun edgeBottomFade(): Dp {
    val configuration = LocalConfiguration.current
    return if (configuration.isScreenRound) configuration.screenHeightDp.dp * ROUND_BOTTOM_FADE else 0.dp
}

fun Modifier.fadingBottomEdge(height: Dp): Modifier =
    if (height <= 0.dp) this
    else graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val fade = height.toPx().coerceAtMost(size.height)
            val top = size.height - fade
            drawRect(
                brush = Brush.verticalGradient(listOf(Color.Black, Color.Transparent), startY = top, endY = size.height),
                topLeft = Offset(0f, top),
                size = Size(size.width, fade),
                blendMode = BlendMode.DstIn
            )
        }
