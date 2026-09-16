package org.sparcs.soap.timetableTests

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.helpers.TimetablePhotoPalette
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import kotlin.math.max
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class TimetablePhotoPaletteTest {
    @Test fun `dark light saturated and flat images produce readable complete palettes`() = runTest {
        listOf(
            listOf("102033", "304C62", "68424C", "273F35"),
            listOf("FFE3BB", "C3DDBA", "AFC9E8", "E4B4C8"),
            listOf("FF0000", "00FF00", "0000FF", "FFFF00"),
            listOf("000000"), listOf("FFFFFF"), listOf("777777")
        ).forEach { colors ->
            val palette = TimetablePhotoPalette.generate(pixels(colors))
            assertEquals(8, palette.colors.size)
            assertTrue(palette.colors.distinct().size >= 4)
            assertTrue(palette.applyTo(TimetableTheme.Default).isValid)
            palette.colors.forEach { color ->
                assertTrue("$color on ${palette.text}", contrast(color, palette.text) >= 4.5)
                assertTrue(contrast(color, palette.background) >= 1.2)
            }
            assertTrue(contrast(palette.background, palette.gridLabel) >= 4.5)
            assertNotEquals(palette.background, palette.separator)
        }
    }

    @Test fun `preserves appearance and distinct photo hues`() = runTest {
        val dark = TimetablePhotoPalette.generate(pixels(listOf("182B43", "283F5C")))
        val light = TimetablePhotoPalette.generate(pixels(listOf("FFEEDD", "E6DACB")))
        assertTrue(luminance(dark.background) < 0.06)
        assertTrue(luminance(dark.text) > 0.85)
        assertTrue(luminance(light.background) > 0.75)
        assertTrue(luminance(light.text) < 0.05)
        val hues = TimetablePhotoPalette.generate(pixels(listOf("993333", "336633", "333399"))).colors
        for (channel in 0..2) assertTrue(hues.any { hex ->
            val rgb = channels(hex)
            (0..2).filter { it != channel }.all { rgb[channel] > rgb[it] * 1.3 }
        })
    }

    @Test fun `background retains the dominant photo color`() = runTest {
        val palette = TimetablePhotoPalette.generate(pixels(listOf("1E5A5A", "1E5A5A", "1E5A5A", "9A3B2C")))
        val dominant = oklch("1E5A5A")
        val background = oklch(palette.background)
        assertTrue(hueDistance(background, dominant) < 0.2)
        assertTrue(background[1] > 0.02)
        assertTrue(abs(background[0] - dominant[0]) < 0.25)
    }

    @Test fun `cell shades stay near the photo hues`() = runTest {
        val source = listOf("2F6DB5", "B5502F", "3F8F3F")
        val palette = TimetablePhotoPalette.generate(pixels(source))
        val hues = source.map(::oklch)
        palette.colors.map(::oklch).filter { it[1] > 0.02 }.forEach { color ->
            assertTrue(hues.any { hueDistance(color, it) < 0.35 })
        }
    }

    @Test fun `generation is deterministic and ignores transparent margins`() = runTest {
        val source = pixels(listOf("9D654D", "38605C", "D8B888"))
        assertEquals(TimetablePhotoPalette.generate(source), TimetablePhotoPalette.generate(source))
        val tiny = pixels(listOf("315D78"))
        assertEquals(TimetablePhotoPalette.generate(tiny), TimetablePhotoPalette.generate(tiny + intArrayOf(0, 0, 0)))
    }

    @Test fun `empty and transparent images fail without changing a draft`() = runTest {
        for (pixels in listOf(intArrayOf(), intArrayOf(0, 0x123456))) {
            var failed = false
            try { TimetablePhotoPalette.generate(pixels) } catch (error: IllegalArgumentException) { failed = true }
            assertTrue(failed)
        }
        val original = TimetableTheme.Default.duplicate("My Theme")
        val generated = TimetablePhotoPalette.generate(pixels(listOf("315D78"))).applyTo(original)
        assertEquals(original.id, generated.id)
        assertEquals(original.name, generated.name)
        assertTrue(generated.isValid)
    }

    @Test fun `cancelled generation does not produce a palette`() = runTest {
        var cancelled = false
        val job = launch {
            cancel()
            try { TimetablePhotoPalette.generate(pixels(listOf("123456"))) }
            catch (error: CancellationException) { cancelled = true }
        }
        job.join()
        assertTrue(cancelled)
    }

    private fun pixels(colors: List<String>) = colors.map { (0xFF000000L or it.toLong(16)).toInt() }.toIntArray()
    private fun oklch(hex: String): DoubleArray {
        val rgb = channels(hex).map { if (it <= 0.04045) it / 12.92 else ((it + 0.055) / 1.055).pow(2.4) }
        val l = cbrt(0.4122214708 * rgb[0] + 0.5363325363 * rgb[1] + 0.0514459929 * rgb[2])
        val m = cbrt(0.2119034982 * rgb[0] + 0.6806995451 * rgb[1] + 0.1073969566 * rgb[2])
        val s = cbrt(0.0883024619 * rgb[0] + 0.2817188376 * rgb[1] + 0.6299787005 * rgb[2])
        val a = 1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s
        val b = 0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s
        return doubleArrayOf(0.2104542553 * l + 0.7936177850 * m - 0.0040720468 * s, sqrt(a * a + b * b), atan2(b, a))
    }
    private fun hueDistance(a: DoubleArray, b: DoubleArray): Double {
        val delta = abs(a[2] - b[2])
        return min(delta, 2 * PI - delta)
    }
    private fun channels(hex: String): List<Double> {
        val value = hex.toInt(16)
        return listOf((value shr 16 and 255) / 255.0, (value shr 8 and 255) / 255.0, (value and 255) / 255.0)
    }
    private fun luminance(hex: String): Double {
        val rgb = channels(hex).map { if (it <= 0.04045) it / 12.92 else ((it + 0.055) / 1.055).pow(2.4) }
        return 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2]
    }
    private fun contrast(a: String, b: String): Double =
        (max(luminance(a), luminance(b)) + 0.05) / (min(luminance(a), luminance(b)) + 0.05)
}
