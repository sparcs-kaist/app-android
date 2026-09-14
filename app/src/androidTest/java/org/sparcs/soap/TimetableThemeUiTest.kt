package org.sparcs.soap

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.models.otl.LectureItem
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.app.features.timetable.components.TimetableGridCell
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.sparcs.soap.app.domain.helpers.TimetableThemeStore
import org.sparcs.soap.app.features.settings.timetable.TimetableThemeSettingsView
import org.sparcs.soap.app.theme.ui.Theme

class TimetableThemeUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int) = context.getString(id)
    private var originalPreferences: Map<String, *> = emptyMap<String, String>()
    @Before fun setup() {
        originalPreferences = context.getSharedPreferences("timetable_themes", 0).all.toMap()
        context.getSharedPreferences("timetable_themes", 0).edit().clear().commit()
        compose.runOnUiThread { compose.activity.setContent { Theme { TimetableThemeSettingsView {} } } }
    }
    @After fun restorePreferences() {
        context.getSharedPreferences("timetable_themes", 0).edit().clear().apply {
            originalPreferences.forEach { (key, value) -> if (value is String) putString(key, value) }
        }.commit()
    }
    private fun screenshot(name: String) {
        compose.waitForIdle()
        java.io.File(context.getExternalFilesDir(null), "$name.png").outputStream().use {
            InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    @Test fun selectDuplicateEditAndDeleteTheme() {
        compose.onNodeWithText(text(R.string.theme_legacy)).performScrollTo().performClick()
        compose.runOnIdle { assertEquals("builtin.legacy", TimetableThemeStore(context).state.selectedID) }
        screenshot("theme-collections")
        compose.onNodeWithContentDescription(context.getString(R.string.theme_options, text(R.string.theme_legacy))).performClick()
        compose.onNodeWithText(text(R.string.theme_duplicate)).performClick()
        compose.onNodeWithText(text(R.string.theme_name)).performTextClearance()
        compose.onNodeWithText(text(R.string.theme_name)).performTextInput("My Palette")
        compose.onNodeWithContentDescription(context.getString(R.string.theme_color, 1)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.theme_hex)).performTextClearance()
        compose.onNodeWithText(text(R.string.theme_hex)).performTextInput("ABCDEF")
        screenshot("theme-color-picker")
        compose.onNodeWithText(text(R.string.theme_apply)).performClick()
        compose.onNodeWithText(text(R.string.theme_advanced)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.theme_background)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.theme_change_color)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.theme_hex)).performTextClearance()
        compose.onNodeWithText(text(R.string.theme_hex)).performTextInput("0B1622")
        compose.onNodeWithText(text(R.string.theme_apply)).performClick()
        screenshot("theme-advanced")
        compose.onNodeWithText(text(R.string.theme_save)).performClick()
        compose.runOnIdle {
            val theme = TimetableThemeStore(context).state.selected
            assertEquals("My Palette", theme.name)
            assertEquals("ABCDEF", theme.hexColors.first())
            assertEquals("0B1622", theme.backgroundColorHex)
            assertFalse(theme.isBuiltIn)
        }
        compose.onNodeWithText("My Palette").performScrollTo()
        compose.onNodeWithContentDescription(context.getString(R.string.theme_options, "My Palette")).performClick()
        compose.onNodeWithText(text(R.string.delete)).performClick()
        compose.onNodeWithText(text(R.string.delete)).performClick()
        compose.runOnIdle { assertEquals("builtin.default", TimetableThemeStore(context).state.selectedID) }
    }

    @Test fun cancelEditorDoesNotChangeSelectedTheme() {
        compose.onNodeWithText(text(R.string.theme_new)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.theme_name)).performTextClearance()
        compose.onNodeWithText(text(R.string.theme_save)).assertIsNotEnabled()
        compose.onNodeWithContentDescription(text(R.string.back)).performClick()
        compose.onNodeWithText(text(R.string.theme_discard)).performClick()
        compose.runOnIdle { assertTrue(TimetableThemeStore(context).state.customThemes.isEmpty()) }
    }
    @Test fun selectedPaletteRecolorsAnExistingCellImmediately() {
        val item = LectureItem.mockList().first()
        compose.runOnUiThread {
            compose.activity.setContent {
                Theme { TimetableGridCell(item, false, 100.dp, Modifier.width(100.dp).testTag("cell")) }
            }
        }
        compose.runOnIdle { TimetableThemeStore(context).select("builtin.legacy") }
        compose.waitForIdle()
        val pixels = compose.onNodeWithTag("cell").captureToImage().toPixelMap()
        assertEquals(TimetableTheme.builtIn[1].colorFor(item.lecture.courseID), pixels[1, pixels.height / 2])
    }

}
