package org.sparcs.soap

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.features.settings.timetable.TimetableThemeEditor
import org.sparcs.soap.app.theme.ui.Theme

class TimetableThemeEditorPolicyTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()
    private val seed = TimetableTheme.Default.duplicate("Test palette")
        .copy(hexColors = listOf("112233", "445566", "778899"))
    private val saved = mutableListOf<TimetableTheme>()

    private fun showEditor(theme: TimetableTheme = seed) {
        compose.activity.setContent {
            Theme {
                TimetableThemeEditor(Json.encodeToString(theme), onBack = {}, onSave = saved::add)
            }
        }
    }

    private fun openColorMenu(index: Int) {
        val label = compose.activity.getString(R.string.theme_color, index)
        val options = compose.activity.getString(R.string.theme_options, label)
        compose.onNodeWithContentDescription(options).performScrollTo().performClick()
    }

    @Test
    fun reorderWaitsForDoneBeforeSaving() {
        showEditor()
        val first = compose.onNodeWithContentDescription(compose.activity.getString(R.string.theme_drag_color, 1))
        first.performScrollTo()
        val second = compose.onNodeWithContentDescription(compose.activity.getString(R.string.theme_drag_color, 2))
        val destination = second.fetchSemanticsNode().boundsInRoot.center - first.fetchSemanticsNode().boundsInRoot.topLeft
        first.performTouchInput {
            down(center)
            moveTo(destination, 600)
            up()
        }

        compose.mainClock.advanceTimeBy(1_000)
        compose.runOnIdle { assertTrue(saved.isEmpty()) }
        compose.onNodeWithText(compose.activity.getString(R.string.done)).performClick()
        compose.runOnIdle { assertEquals(listOf("445566", "112233", "778899"), saved.single().hexColors) }
        compose.onNodeWithText(compose.activity.getString(R.string.theme_name)).performScrollTo().assertExists()
    }

    @Test
    fun lastColorCannotBeDeletedAndCanBeExtended() {
        showEditor(seed.copy(hexColors = listOf("112233")))
        openColorMenu(1)
        compose.onNodeWithText(compose.activity.getString(R.string.theme_remove_color)).assertIsNotEnabled()
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText(compose.activity.getString(R.string.theme_add_color)).performScrollTo().performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.done)).performClick()

        compose.runOnIdle { assertEquals(2, saved.last().hexColors.size) }
    }

    @Test
    fun untouchedDuplicateIsOnlyCreatedWhenConfirmed() {
        showEditor()
        compose.waitForIdle()
        compose.runOnIdle { assertTrue(saved.isEmpty()) }
        compose.onNodeWithText(compose.activity.getString(R.string.done)).performClick()

        compose.runOnIdle { assertEquals(listOf(seed), saved) }
    }

    @Test
    fun deletionAndSubsequentEditsWaitForDone() {
        showEditor()
        openColorMenu(1)
        compose.onNodeWithText(compose.activity.getString(R.string.theme_remove_color)).performClick()
        compose.mainClock.advanceTimeBy(1_000)
        compose.runOnIdle { assertTrue(saved.isEmpty()) }
        compose.onNodeWithText(compose.activity.getString(R.string.theme_add_color)).performScrollTo().performClick()
        compose.mainClock.advanceTimeBy(1_000)
        compose.runOnIdle { assertTrue(saved.isEmpty()) }
        compose.onNodeWithText(compose.activity.getString(R.string.done)).performClick()
        compose.runOnIdle {
            assertEquals(1, saved.size)
            assertEquals("445566", saved.single().hexColors.first())
        }
    }
}
