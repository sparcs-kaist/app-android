package org.sparcs.soap.app.domain.helpers

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object TimetablePhotoPalette {
    private const val ReadableContrast = 4.7
    private const val CellCount = 8
    data class Palette(
        val colors: List<String>, val text: String, val background: String,
        val separator: String, val gridLabel: String,
    ) {
        fun applyTo(theme: TimetableTheme) = theme.copy(
            hexColors = colors, textColorHex = text, backgroundColorHex = background,
            separatorColorHex = separator, gridLabelColorHex = gridLabel
        )
    }

    suspend fun generate(pixels: IntArray): Palette {
        currentCoroutineContext().ensureActive()
        val buckets = sortedMapOf<Int, Accumulator>()
        for (pixel in pixels) {
            val alpha = (pixel ushr 24) / 255.0
            if (alpha < 0.5) continue
            val rgb = RGB((pixel shr 16 and 255) / 255.0, (pixel shr 8 and 255) / 255.0, (pixel and 255) / 255.0)
            val key = ((rgb.r * 31).toInt() shl 10) or ((rgb.g * 31).toInt() shl 5) or (rgb.b * 31).toInt()
            buckets.getOrPut(key) { Accumulator() }.add(rgb, alpha)
        }
        require(buckets.isNotEmpty()) { "Unreadable image" }
        val samples = buckets.values.map { it.sample() }
        var centers = listOf(samples.maxBy { it.weight }.perceptual)
        while (centers.size < min(10, samples.size)) {
            val next = samples.maxBy { sample ->
                centers.minOf { distance(sample.perceptual, it) } * sqrt(sample.weight)
            }
            if (centers.minOf { distance(next.perceptual, it) } <= 0.0004) break
            centers = centers + listOf(next.perceptual)
        }
        var clusters = emptyList<Sample>()
        for (iteration in 0 until 12) {
            currentCoroutineContext().ensureActive()
            val groups = List(centers.size) { Accumulator() }
            samples.forEach { sample ->
                val closest = centers.indices.minBy { distance(sample.perceptual, centers[it]) }
                groups[closest].add(sample.color, sample.weight)
            }
            clusters = groups.filter { it.weight > 0 }.map { it.sample() }
            val updated = clusters.map { it.perceptual }
            val converged = centers.size == updated.size && centers.zip(updated).all { (a, b) -> distance(a, b) < 0.000001 }
            centers = updated
            if (converged) break
        }
        clusters = merged(clusters.sortedByDescending { it.weight })
        currentCoroutineContext().ensureActive()
        val totalWeight = samples.sumOf { it.weight }
        val averageLightness = samples.sumOf { it.perceptual[0] * it.weight } / totalWeight
        val dominant = clusters.first().color.oklab
        val dark = 0.7 * dominant.lightness + 0.3 * averageLightness < 0.58
        val background = Oklab.fromLch(
            if (dark) (dominant.lightness * 0.62).coerceIn(0.10, 0.30)
            else (1 - (1 - dominant.lightness) * 0.55).coerceIn(0.84, 0.96),
            if (dark) min(dominant.chroma * 0.9, 0.085) else min(dominant.chroma * 0.75, 0.055),
            dominant.hue
        )
        val text = Oklab.fromLch(if (dark) 0.985 else 0.16, min(dominant.chroma * 0.25, 0.02), dominant.hue)
        val separator = Oklab.fromLch(
            if (dark) min(background.lightness + 0.13, 0.55) else max(background.lightness - 0.11, 0.45),
            background.chroma, background.hue
        )
        val gridLabel = Oklab.fromLch(
            if (dark) 0.84 else 0.36, min(dominant.chroma * 0.5, 0.05), dominant.hue
        ).contrasting(background.rgb, darker = !dark)
        val seeds = mutableListOf<Oklab>()
        for (cluster in clusters.filter { it.weight / totalWeight >= 0.01 }) {
            val color = cluster.color.oklab
            if (seeds.all { sqrt((it.a - color.a).pow(2) + (it.b - color.b).pow(2)) >= 0.03 }) {
                seeds.add(color)
            }
        }
        return Palette(cellColors(seeds, background, text, dark), text.rgb.hex, background.rgb.hex, separator.rgb.hex, gridLabel.rgb.hex)
    }

    private fun merged(clusters: List<Sample>): List<Sample> {
        val groups = mutableListOf<Pair<DoubleArray, Accumulator>>()
        for (cluster in clusters) {
            val group = groups.firstOrNull { distance(it.first, cluster.perceptual) < 0.0025 }
            val accumulator = group?.second ?: Accumulator().also { groups.add(cluster.perceptual to it) }
            accumulator.add(cluster.color, cluster.weight)
        }
        return groups.map { it.second.sample() }.sortedByDescending { it.weight }
    }

    private fun cellColors(seeds: List<Oklab>, background: Oklab, text: Oklab, dark: Boolean): List<String> {
        val limit = if (dark) (text.rgb.luminance + 0.05) / ReadableContrast - 0.05
        else (text.rgb.luminance + 0.05) * ReadableContrast - 0.05
        val edge = cbrt(limit.coerceIn(0.0, 1.0))
        val near = if (dark) max(background.lightness + 0.14, 0.30) else min(background.lightness - 0.11, 0.88)
        val far = if (dark) max(edge, near + 0.06) else min(edge, near - 0.06)
        val lower = min(near, far)
        val upper = max(near, far)
        val ranks = DoubleArray(seeds.size) { 0.5 }
        if (seeds.size > 1) {
            seeds.indices.sortedBy { seeds[it].lightness }.forEachIndexed { position, index ->
                ranks[index] = position.toDouble() / (seeds.size - 1)
            }
        }
        val rounds = (CellCount + seeds.size - 1) / seeds.size
        val offsets = if (rounds == 1) listOf(0.0) else (0 until rounds)
            .map { -0.5 + it.toDouble() / (rounds - 1) }
            .sortedWith(compareBy<Double> { abs(it) }.thenByDescending { it })
        val anchor = min(0.8, 1.0 / rounds)
        return List(CellCount) { index ->
            val seed = seeds[index % seeds.size]
            val round = index / seeds.size
            val tone = (anchor * ranks[index % seeds.size] + (1 - anchor) * (0.5 + offsets[round] * 0.95)).coerceIn(0.0, 1.0)
            val chroma = if (seed.chroma < 0.012) seed.chroma else min(seed.chroma * max(1.15 - 0.1 * round, 0.7), 0.16)
            Oklab.fromLch(lower + (upper - lower) * tone, chroma, seed.hue + 0.5 * offsets[round])
                .contrasting(text.rgb, darker = dark).rgb.hex
        }
    }

    private fun distance(a: DoubleArray, b: DoubleArray): Double = a.indices.sumOf { (a[it] - b[it]).pow(2) }

    private class Sample(val color: RGB, val weight: Double) {
        val perceptual = color.perceptual
    }

    private class Accumulator {
        var weight = 0.0
        private var r = 0.0
        private var g = 0.0
        private var b = 0.0
        fun add(color: RGB, weight: Double) {
            r += color.r * weight
            g += color.g * weight
            b += color.b * weight
            this.weight += weight
        }
        fun sample() = Sample(RGB(r / weight, g / weight, b / weight), weight)
    }

    private data class Oklab(val lightness: Double, val a: Double, val b: Double) {
        val chroma get() = sqrt(a * a + b * b)
        val hue get() = atan2(b, a)
        val rgb: RGB get() {
            RGB.fromOklab(this)?.let { return it }
            var lower = 0.0
            var upper = 1.0
            repeat(12) {
                val amount = (lower + upper) / 2
                if (RGB.fromOklab(copy(a = a * amount, b = b * amount)) != null) lower = amount else upper = amount
            }
            return RGB.fromOklab(copy(a = a * lower, b = b * lower), clamp = true)!!
        }

        fun contrasting(other: RGB, darker: Boolean): Oklab {
            val target = if (darker) (other.luminance + 0.05) / ReadableContrast - 0.05
            else (other.luminance + 0.05) * ReadableContrast - 0.05
            if (if (darker) rgb.luminance <= target else rgb.luminance >= target) return this
            var lower = 0.0
            var upper = 1.0
            repeat(20) {
                val candidate = (lower + upper) / 2
                if (copy(lightness = candidate).rgb.luminance < target.coerceIn(0.0, 1.0)) lower = candidate else upper = candidate
            }
            return copy(lightness = (lower + upper) / 2)
        }

        companion object {
            fun fromLch(lightness: Double, chroma: Double, hue: Double) = Oklab(lightness, cos(hue) * chroma, sin(hue) * chroma)
        }
    }

    private data class RGB(val r: Double, val g: Double, val b: Double) {
        private fun linear(value: Double) = if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        val luminance get() = 0.2126 * linear(r) + 0.7152 * linear(g) + 0.0722 * linear(b)
        val perceptual: DoubleArray get() {
            val red = linear(r)
            val green = linear(g)
            val blue = linear(b)
            val l = cbrt(0.4122214708 * red + 0.5363325363 * green + 0.0514459929 * blue)
            val m = cbrt(0.2119034982 * red + 0.6806995451 * green + 0.1073969566 * blue)
            val s = cbrt(0.0883024619 * red + 0.2817188376 * green + 0.6299787005 * blue)
            return doubleArrayOf(
                0.2104542553 * l + 0.7936177850 * m - 0.0040720468 * s,
                1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s,
                0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s
            )
        }
        val oklab get() = perceptual.let { Oklab(it[0], it[1], it[2]) }
        val hex get() = String.format(Locale.ROOT, "%02X%02X%02X", (r * 255).roundToInt(), (g * 255).roundToInt(), (b * 255).roundToInt())
        companion object {
            fun fromOklab(color: Oklab, clamp: Boolean = false): RGB? {
                val l = (color.lightness + 0.3963377774 * color.a + 0.2158037573 * color.b).pow(3)
                val m = (color.lightness - 0.1055613458 * color.a - 0.0638541728 * color.b).pow(3)
                val s = (color.lightness - 0.0894841775 * color.a - 1.2914855480 * color.b).pow(3)
                val channels = doubleArrayOf(
                    4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s,
                    -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s,
                    -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s
                )
                if (!clamp && channels.any { it < -0.0005 || it > 1.0005 }) return null
                fun encode(channel: Double): Double {
                    val value = channel.coerceIn(0.0, 1.0)
                    return if (value <= 0.0031308) value * 12.92 else 1.055 * value.pow(1 / 2.4) - 0.055
                }
                return RGB(encode(channels[0]), encode(channels[1]), encode(channels[2]))
            }
        }
    }
}
