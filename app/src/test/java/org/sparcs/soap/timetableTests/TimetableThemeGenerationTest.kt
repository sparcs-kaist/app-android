package org.sparcs.soap.timetableTests

import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.helpers.TimetablePhotoPalette
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.helpers.TimetableThemeBrief
import org.sparcs.soap.app.domain.usecases.ThemeBriefParser
import org.sparcs.soap.app.domain.usecases.ThemeGenerationError
import org.sparcs.soap.app.domain.usecases.ThemeGenerationException
import org.sparcs.soap.app.domain.usecases.ThemeGenerationUseCase
import org.sparcs.soap.app.domain.usecases.ThemeModelStatus
import org.sparcs.soap.app.features.settings.timetable.TimetableThemeGeneratorViewModel
import org.sparcs.soap.testSupport.MainDispatcherRule
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

@OptIn(ExperimentalCoroutinesApi::class)
class TimetableThemeGenerationTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val base = TimetableTheme.Default.duplicate("My Theme")
    private val brief = TimetableThemeBrief("가을", TimetableThemeBrief.Appearance.LIGHT, listOf("C8102E", "1B7A3D"))

    @Test fun `streaming parser waits for closed strings and validates final response`() {
        val partial = ThemeBriefParser.partial("""{"name":"가을","appearance":"light","colors":["C8102E","1B7""")
        assertEquals(listOf("C8102E"), partial.anchorHexColors)
        assertEquals("가을", partial.name)
        assertTrue(partial.theme(base)!!.isValid)
        assertNull(ThemeBriefParser.partial("""{"name":"Half","colors":["C8102E"]}""").theme(base))
        assertEquals(brief, ThemeBriefParser.complete("""{"name":"가을","appearance":"light","colors":["C8102E","1B7A3D"]}"""))
        for (invalid in listOf("", "I cannot do that", """{"appearance":"light","colors":["GGGGGG"]}""", """{"appearance":"dark","colors":["123456"]""")) {
            try {
                ThemeBriefParser.complete(invalid)
                throw AssertionError("Accepted incomplete model response")
            } catch (error: ThemeGenerationException) { assertEquals(ThemeGenerationError.INCOMPLETE, error.reason) }
        }
    }

    @Test fun `normalizes valid colors and discards malformed ones`() {
        assertEquals("AABBCC", TimetableThemeBrief.normalizeHex(" #abc "))
        assertEquals("C8102E", TimetableThemeBrief.normalizeHex("c8102e"))
        listOf("", "C810", "GGGGGG", "C8102E00").forEach { assertNull(TimetableThemeBrief.normalizeHex(it)) }
        assertEquals(
            TimetablePhotoPalette.fromBrief(brief.copy(anchorHexColors = listOf("C8102E"))),
            TimetablePhotoPalette.fromBrief(brief.copy(anchorHexColors = listOf("#C8102E", "c8102e", "bad color"))),
        )
    }

    @Test fun `complete JSON surrounded by model commentary remains usable`() {
        val json = """{"name":"가을","appearance":"light","colors":["C8102E","1B7A3D"]}"""
        assertEquals(brief, ThemeBriefParser.complete("Here is your theme:\n```JSON\n$json\n```\nEnjoy!"))
        val quotedBrace = """{"name":"A } B","appearance":"light","colors":["C8102E"]}"""
        assertEquals("A } B", ThemeBriefParser.complete(quotedBrace).name)
    }

    @Test fun `transient and format failures retry once without showing an error`() = runTest {
        for (reason in listOf(ThemeGenerationError.BUSY, ThemeGenerationError.INCOMPLETE)) {
            var attempts = 0
            val model = readyModel(FakeUseCase(flow {
                attempts++
                if (attempts == 1) throw ThemeGenerationException(reason)
                emit(brief)
            }))
            model.generate(base, "prompt")
            assertTrue(model.state.value.generating)
            assertNull(model.state.value.error)
            advanceUntilIdle()
            assertEquals(2, attempts)
            assertTrue(model.state.value.ready)
            assertNull(model.state.value.error)
        }
    }

    @Test fun `persistent failures stop after one retry and cancellation prevents retry`() = runTest {
        var attempts = 0
        val model = readyModel(FakeUseCase(flow {
            attempts++
            throw ThemeGenerationException(ThemeGenerationError.BUSY)
        }))
        model.generate(base, "prompt")
        advanceUntilIdle()
        assertEquals(2, attempts)
        assertFalse(model.state.value.generating)
        assertEquals(ThemeGenerationError.BUSY, model.state.value.error)
        model.generate(base, "prompt")
        model.cancel()
        advanceUntilIdle()
        assertEquals(3, attempts)
        assertFalse(model.state.value.generating)
        assertNull(model.state.value.error)
    }

    @Test fun `sixteen colors remain readable in both appearances and preserve identity`() {
        val inputs = listOf(
            listOf("C8102E", "1B7A3D", "A00C24", "145E2F"),
            listOf("4C9A2A", "7FD67A", "2E6B1F"),
            listOf("E63946", "F3722C", "F9C74F", "43AA8B", "277DA1", "7209B7"),
            listOf("0B0B0F", "2D1B4E"), listOf("FFE066"), listOf("777777", "808080"),
        )
        for (appearance in TimetableThemeBrief.Appearance.entries) for (colors in inputs) {
            val request = brief.copy(appearance = appearance, anchorHexColors = colors)
            val palette = TimetablePhotoPalette.fromBrief(request)!!
            assertEquals(16, palette.colors.size)
            assertEquals(16, palette.colors.distinct().size)
            palette.colors.forEach { color ->
                assertTrue("$color text", contrast(color, palette.text) >= 4.5)
                assertTrue("$color background", contrast(color, palette.background) >= 1.2)
            }
            assertTrue(contrast(palette.background, palette.gridLabel) >= 4.5)
            assertNotEquals(palette.background, palette.separator)
            assertEquals(palette, TimetablePhotoPalette.fromBrief(request))
            val theme = request.theme(base)!!
            assertEquals(base.id, theme.id)
            assertFalse(theme.isBuiltIn)
            assertTrue(theme.isValid)
        }
        val dark = TimetablePhotoPalette.fromBrief(brief.copy(appearance = TimetableThemeBrief.Appearance.DARK))!!
        val light = TimetablePhotoPalette.fromBrief(brief)!!
        assertTrue(luminance(dark.background) < luminance(light.background))
        val green = brief.copy(anchorHexColors = listOf("4C9A2A", "7FD67A", "2E6B1F")).theme(base)!!
        green.hexColors.forEach { hex ->
            val rgb = hex.toInt(16)
            assertTrue((rgb shr 8 and 255) > (rgb shr 16 and 255))
            assertTrue((rgb shr 8 and 255) > (rgb and 255))
        }
    }

    @Test fun `generation is preview only until complete and cancellation clears partial result`() = runTest {
        val useCase = FakeUseCase(flow { emit(brief); awaitCancellation() })
        val model = readyModel(useCase)
        model.generate(base, "prompt")
        assertTrue(model.state.value.generating)
        assertFalse(model.state.value.ready)
        assertEquals(base.id, model.state.value.preview!!.id)
        model.cancel()
        assertFalse(model.state.value.generating)
        assertFalse(model.state.value.ready)
        assertNull(model.state.value.preview)
        assertEquals("My Theme", base.name)
    }

    @Test fun `completed result can be regenerated and editing description invalidates it`() = runTest {
        val model = readyModel(FakeUseCase(flowOf(brief)))
        model.generate(base, "prompt")
        assertTrue(model.state.value.ready)
        assertEquals("가을", model.state.value.preview!!.name)
        model.generate(base, "prompt")
        assertTrue(model.state.value.ready)
        model.describe("x".repeat(200))
        assertEquals(120, model.state.value.description.length)
        assertFalse(model.state.value.ready)
        assertNull(model.state.value.preview)
        model.reset()
        assertEquals("", model.state.value.description)
    }

    @Test fun `every generation uses a positive seed accepted by Gemini Nano`() = runTest {
        val useCase = FakeUseCase(flowOf(brief))
        val model = readyModel(useCase)
        repeat(100) { model.generate(base, "prompt") }
        assertEquals(100, useCase.seeds.size)
        assertTrue(useCase.seeds.all { it > 0 })
        useCase.seeds.forEach { variation ->
            assertEquals(variation, generateContentRequest(TextPart("theme")) { seed = variation }.seed)
        }
    }

    @Test fun `negative seed reproduces SDK rejection before model generation`() {
        assertThrows(IllegalArgumentException::class.java) {
            generateContentRequest(TextPart("theme")) { seed = -1 }
        }
    }

    @Test fun `failure after preview removes applyable result and allows retry`() = runTest {
        val model = readyModel(FakeUseCase(flow {
            emit(brief)
            throw ThemeGenerationException(ThemeGenerationError.UNSAFE_REQUEST)
        }))
        model.generate(base, "prompt")
        assertFalse(model.state.value.ready)
        assertNull(model.state.value.preview)
        assertEquals(ThemeGenerationError.UNSAFE_REQUEST, model.state.value.error)
        assertTrue(model.state.value.canGenerate)
    }

    @Test fun `unsupported and downloadable devices cannot generate until ready`() = runTest {
        val useCase = FakeUseCase(flowOf(brief), ThemeModelStatus.UNAVAILABLE)
        val model = readyModel(useCase)
        model.generate(base, "prompt")
        assertEquals(0, useCase.requests)
        useCase.status = ThemeModelStatus.DOWNLOADABLE
        model.refreshAvailability()
        assertFalse(model.state.value.canGenerate)
        model.download()
        assertTrue(model.state.value.canGenerate)
        model.generate(base, "prompt")
        assertEquals(1, useCase.requests)
    }

    private fun readyModel(useCase: FakeUseCase) = TimetableThemeGeneratorViewModel(useCase).apply {
        refreshAvailability()
        describe("autumn")
    }

    @Test fun `battery quota errors are shown without automatic retry`() = runTest {
        val useCase = FakeUseCase(flow { throw ThemeGenerationException(ThemeGenerationError.QUOTA_EXCEEDED) })
        val model = readyModel(useCase)
        model.generate(base, "prompt")
        advanceUntilIdle()
        assertEquals(1, useCase.requests)
        assertEquals(ThemeGenerationError.QUOTA_EXCEEDED, model.state.value.error)
        assertFalse(model.state.value.generating)
    }

    private class FakeUseCase(val stream: Flow<TimetableThemeBrief>, var status: ThemeModelStatus = ThemeModelStatus.AVAILABLE) : ThemeGenerationUseCase {
        var requests = 0
        val seeds = mutableListOf<Int>()
        override suspend fun availability() = status
        override suspend fun download() { status = ThemeModelStatus.AVAILABLE }
        override fun generate(prompt: String, variation: Int): Flow<TimetableThemeBrief> {
            requests++
            seeds.add(variation)
            return stream
        }
    }

    private fun luminance(hex: String): Double {
        val value = hex.toInt(16)
        val rgb = listOf(value shr 16 and 255, value shr 8 and 255, value and 255).map {
            val channel = it / 255.0
            if (channel <= 0.04045) channel / 12.92 else ((channel + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2]
    }

    private fun contrast(a: String, b: String) = (max(luminance(a), luminance(b)) + 0.05) / (min(luminance(a), luminance(b)) + 0.05)
}
