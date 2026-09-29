package org.sparcs.soap.app.features.friends.addFriends.components

import android.graphics.Bitmap
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * The Add Friends backdrop, ported from iOS's `AnimatedMeshGradientView`: a
 * near-black top that three coloured columns (red, blue, purple) rise into,
 * taking turns to reach for the top.
 *
 * Compose has no mesh gradient, so each frame the same 3×3 grid is
 * interpolated into a small bitmap and scaled up with bilinear filtering,
 * which keeps it smooth and cheap without needing AGSL (API 33+).
 */
@Composable
fun AnimatedMeshGradient(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // Honour "Remove animations": draw one still frame instead.
    val animates = remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
    var seconds by remember { mutableDoubleStateOf(wallClockSeconds()) }
    LaunchedEffect(animates) {
        if (!animates) return@LaunchedEffect
        while (true) withFrameNanos { seconds = wallClockSeconds() }
    }

    val renderer = remember { MeshGradientRenderer() }
    Canvas(modifier.fillMaxSize()) {
        // Reading `seconds` here only invalidates drawing, not composition.
        val image = renderer.render(seconds)
        drawImage(
            image = image,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(image.width, image.height),
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
            filterQuality = FilterQuality.Low
        )
    }
}

/** Like iOS, driven by the wall clock so each visit starts somewhere different. */
private fun wallClockSeconds(): Double = (System.currentTimeMillis() % 100_000_000L) / 1_000.0

private class MeshGradientRenderer {
    private val width = 36
    private val height = 72
    private val pixels = IntArray(width * height)
    private val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    private val image = bitmap.asImageBitmap()

    fun render(t: Double): ImageBitmap {
        val columnY = doubleArrayOf(columnTop(t, 0.0), columnTop(t, 1.0), columnTop(t, 2.0))
        val blueY = columnY[1]
        val blueX = 0.50 + sin(t * 0.55) * 0.12
        val topX = 0.50 + sin(t * 0.48) * 0.10
        val bottomX = 0.50 + sin(t * 0.51 + 0.8) * 0.12

        for (py in 0 until height) {
            val v = py / (height - 1.0)
            // Where the middle column sits on this row, bending through its
            // top, middle and bottom control points.
            val centerX = if (v < blueY) lerp(topX, blueX, v / blueY)
            else lerp(blueX, bottomX, (v - blueY) / (1 - blueY))

            for (px in 0 until width) {
                val u = px / (width - 1.0)
                val from = if (u < centerX) 0 else 1
                val k = smoothstep(if (from == 0) u / centerX else (u - centerX) / (1 - centerX))
                val to = from + 1

                // The row where this x reaches its column colour.
                val middleY = lerp(columnY[from], columnY[to], k)
                val top = TOP[from].mix(TOP[to], k)
                val middle = MIDDLE[from].mix(MIDDLE[to], k)
                val color = if (v < middleY) top.mix(middle, smoothstep(v / middleY)) else middle
                pixels[py * width + px] = color.argb()
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        image.prepareToDraw()
        return image
    }

    /**
     * Each colour is a column rising from the bottom edge, and the columns take
     * turns reaching for the top. Every column gets the same pulse offset by a
     * third of a cycle; the 4th power keeps the pulse narrow, so only one
     * column is tall at a time. A slow wobble keeps the others from freezing.
     */
    private fun columnTop(t: Double, index: Double): Double {
        val phase = index * 2 * PI / 3
        val pulse = ((1 + cos(t * 0.35 - phase)) / 2).pow(4)
        val wobble = sin(t * 0.27 + phase * 1.7) * 0.04
        return 0.80 - pulse * 0.55 + wobble
    }

    private companion object {
        val TOP = listOf(Rgb(0.01, 0.01, 0.03), Rgb(0.00, 0.00, 0.02), Rgb(0.01, 0.02, 0.05))
        val MIDDLE = listOf(Rgb(0.34, 0.08, 0.24), Rgb(0.07, 0.18, 0.46), Rgb(0.20, 0.07, 0.44))

        fun lerp(a: Double, b: Double, t: Double) = a + (b - a) * t.coerceIn(0.0, 1.0)
        fun smoothstep(t: Double): Double = t.coerceIn(0.0, 1.0).let { it * it * (3 - 2 * it) }
    }

    private data class Rgb(val r: Double, val g: Double, val b: Double) {
        fun mix(other: Rgb, t: Double) = Rgb(lerp(r, other.r, t), lerp(g, other.g, t), lerp(b, other.b, t))
        fun argb(): Int = (0xFF shl 24) or (channel(r) shl 16) or (channel(g) shl 8) or channel(b)
        private fun channel(value: Double) = (value * 255).roundToInt().coerceIn(0, 255)
    }
}

@Preview(widthDp = 360, heightDp = 720)
@Composable
private fun AnimatedMeshGradientPreview() {
    AnimatedMeshGradient()
}
