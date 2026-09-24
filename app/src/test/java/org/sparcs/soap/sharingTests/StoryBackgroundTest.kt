package org.sparcs.soap.sharingTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.shared.sharing.StoryBackground

class StoryBackgroundTest {
    private val theme = TimetableTheme.Default.copy(
        id = "custom.test", hexColors = listOf("123456", "ABCDEF"), backgroundColorHex = "204060"
    )

    @Test fun `palette option preserves current story colors`() {
        assertEquals("#123456" to "#ABCDEF", StoryBackground.ThemeColors.colors(theme))
        assertEquals("#123456" to "#123456", StoryBackground.ThemeColors.colors(theme.copy(hexColors = listOf("123456"))))
    }

    @Test fun `background option follows background independently of palette`() {
        val colors = StoryBackground.ThemeBackground.colors(theme)
        assertEquals("#324F6D" to "#1E3C5A", colors)
        assertEquals(colors, StoryBackground.ThemeBackground.colors(theme.copy(hexColors = listOf("FFFFFF"))))
        assertNotEquals(colors, StoryBackground.ThemeBackground.colors(theme.copy(backgroundColorHex = "FFFFFF")))
    }

    @Test fun `default is stable and missing backgrounds fall back to it`() {
        val colors = StoryBackground.Default.colors(theme)
        assertEquals(colors, StoryBackground.Default.colors(TimetableTheme.Default))
        assertEquals(colors, StoryBackground.ThemeBackground.colors(theme.copy(backgroundColorHex = null)))
        assertEquals(StoryBackground.Default, StoryBackground.initial(TimetableTheme.Default))
        assertEquals(StoryBackground.Default, StoryBackground.initial(TimetableTheme.builtIn.first { it.id == "builtin.legacy" }))
        assertEquals(StoryBackground.ThemeColors, StoryBackground.initial(theme))
    }
}
