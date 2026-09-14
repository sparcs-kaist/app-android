package org.sparcs.soap.timetableTests

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.helpers.TimetableThemeStore

@RunWith(RobolectricTestRunner::class)
class TimetableThemeTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val prefs get() = context.getSharedPreferences("timetable_themes", 0)
    @Before fun clear() { prefs.edit().clear().commit() }

    @Test fun `all eleven collections have valid stable palettes`() {
        assertEquals(11, TimetableTheme.builtIn.size)
        assertEquals(11, TimetableTheme.builtIn.distinctBy { it.id }.size)
        TimetableTheme.builtIn.forEach { assertTrue(it.isValid); assertTrue(it.isBuiltIn); assertEquals(16, it.hexColors.size) }
        assertEquals(Color(0xFFE34B6C), TimetableTheme.Default.colorFor(17))
        assertEquals(TimetableTheme.Default.colorFor(15), TimetableTheme.Default.colorFor(-1))
    }

    @Test fun `save edit and delete active theme survive store recreation`() {
        val theme = TimetableTheme.builtIn[3].duplicate("Blossom").copy(backgroundColorHex = "0B1622", separatorColorHex = "334455", gridLabelColorHex = "ABCDEF")
        TimetableThemeStore(context).saveAndSelect(theme)
        assertEquals(theme, TimetableThemeStore(context).state.selected)
        val edited = theme.copy(name = "Night", textColorHex = "F0F0F0", hexColors = listOf("123456"))
        TimetableThemeStore(context).saveAndSelect(edited)
        assertEquals(listOf(edited), TimetableThemeStore(context).state.customThemes)
        assertEquals(edited, TimetableThemeStore(context).state.selected)
        TimetableThemeStore(context).delete(theme.id)
        assertEquals(TimetableTheme.Default, TimetableThemeStore(context).state.selected)
        assertTrue(TimetableThemeStore(context).state.customThemes.isEmpty())
    }

    @Test fun `preset selection persists and collections cannot be edited or deleted`() {
        val store = TimetableThemeStore(context)
        store.select("builtin.ocean")
        store.delete("builtin.ocean")
        assertEquals("builtin.ocean", TimetableThemeStore(context).state.selected.id)
        assertThrows(IllegalArgumentException::class.java) { store.saveAndSelect(TimetableTheme.Default) }
        assertTrue(store.state.customThemes.isEmpty())
    }

    @Test fun `old custom themes default advanced colors to automatic`() {
        prefs.edit().putString("custom", """[{"id":"custom.old","name":"Old","hexColors":["112233"],"textColorHex":"FFFFFF"}]""")
            .putString("selected", "custom.old").commit()
        val theme = TimetableThemeStore(context).state.selected
        assertEquals("custom.old", theme.id)
        assertNull(theme.backgroundColor)
        assertNull(theme.separatorColor)
        assertNull(theme.gridLabelColor)
    }

    @Test fun `invalid stored themes and dangling selection fall back safely`() {
        prefs.edit().putString("custom", """[{"id":"custom.bad","name":"Bad","hexColors":[],"textColorHex":"bad"}]""")
            .putString("selected", "custom.bad").commit()
        assertEquals(TimetableTheme.Default, TimetableThemeStore(context).state.selected)
        prefs.edit().putString("custom", "broken json").commit()
        assertTrue(TimetableThemeStore(context).state.customThemes.isEmpty())
    }

    @Test fun `label color derives from background independently of cell text`() {
        val theme = TimetableTheme.Default.copy(textColorHex = "000000", backgroundColorHex = "0B1622")
        assertEquals(Color.White, theme.gridLabelColor)
        assertEquals(Color.Black, theme.copy(backgroundColorHex = "EEEEEE").gridLabelColor)
        assertEquals(Color.Red, theme.copy(gridLabelColorHex = "FF0000").gridLabelColor)
    }

    @Test fun `invalid palettes cannot be saved`() {
        val theme = TimetableTheme.Default.duplicate("Custom")
        for (invalid in listOf(theme.copy(name = " "), theme.copy(hexColors = emptyList()), theme.copy(hexColors = List(17) { "FFFFFF" }), theme.copy(textColorHex = "INVALID"))) {
            assertFalse(invalid.isValid)
            assertThrows(IllegalArgumentException::class.java) { TimetableThemeStore(context).saveAndSelect(invalid) }
        }
    }
}
