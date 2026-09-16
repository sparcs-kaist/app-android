package org.sparcs.soap

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.app.features.timetable.TimetableView
import org.sparcs.soap.app.features.timetable.components.TimetableGrid
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel
import java.io.File
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.sparcs.soap.app.domain.models.otl.*
import org.sparcs.soap.app.features.timetable.activity.ActivityCreationView
import org.sparcs.soap.app.theme.ui.Theme

class ActivityCreationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun setContent(content: @Composable () -> Unit) = compose.runOnUiThread { compose.activity.setContent(content = content) }
    private fun screenshot(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    private fun text(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    @Test fun createUsingNativePickerAndSave() {
        var saved: ActivityDraft? = null
        setContent { Theme { ActivityCreationView(Timetable("12", emptyList()), "Spring", onClose = {}, onSave = { saved = it }, onRefresh = {}) } }
        compose.onNodeWithText(text(R.string.activity_save)).assertIsNotEnabled()
        compose.onNodeWithText(text(R.string.activity_title)).performTextInput("Study group")
        compose.onNodeWithText(text(R.string.activity_location)).performTextInput("Library")
        screenshot("activity-form")
        compose.onNodeWithText(text(R.string.activity_starts)).performClick()
        screenshot("activity-time-picker")
        compose.onNodeWithText(text(R.string.activity_keyboard)).assertExists().performClick()
        compose.onNodeWithText(text(R.string.ok)).performClick()
        compose.onNodeWithText(text(R.string.activity_save)).performClick()
        compose.runOnIdle { assertEquals("Study group", saved?.title); assertEquals("Library", saved?.location); assertTrue(saved!!.isValid) }
    }

    @Test fun timetableHasWeekdaysAndOppositeCornerHandles() {
        setContent { Theme { ActivityCreationView(Timetable("12", emptyList()), "Spring", activity = TimetableActivity(17, "Study", "", 0, 540, 600), onClose = {}, onSave = {}, onRefresh = {}) } }
        compose.onNodeWithText(text(R.string.activity_adjust)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.mon)).assertExists()
        compose.onNodeWithText(text(R.string.sat)).assertDoesNotExist()
        compose.onNodeWithText(text(R.string.sun)).assertDoesNotExist()
        screenshot("activity-timetable")
        val start = compose.onAllNodesWithContentDescription(text(R.string.activity_start_handle)).onFirst().fetchSemanticsNode().boundsInRoot
        val end = compose.onAllNodesWithContentDescription(text(R.string.activity_end_handle)).onFirst().fetchSemanticsNode().boundsInRoot
        assertTrue(start.left < end.left)
        assertTrue(start.top < end.top)
        compose.onNodeWithContentDescription(text(R.string.activity_back)).performClick()
        compose.onNodeWithText(text(R.string.activity_save)).assertExists()
    }
    @Test fun draggingSnapsAndAnOverlappingDropKeepsLastValidTime() {
        var saved: ActivityDraft? = null
        val activity = TimetableActivity(17, "Study", "", 0, 540, 600)
        val busy = TimetableActivity(18, "Busy", "", 0, 660, 720)
        setContent { Theme { ActivityCreationView(Timetable("12", emptyList(), listOf(activity, busy)), "Spring", activity,
            onClose = {}, onSave = { saved = it }, onRefresh = {}) } }
        compose.onNodeWithText(text(R.string.activity_adjust)).performScrollTo().performClick()
        compose.onNode(hasContentDescription("Study", substring = true)).performTouchInput {
            down(center); advanceEventTime(700); moveBy(Offset(0f, height.toFloat() * 2)); up()
        }
        screenshot("activity-conflict")
        compose.onNodeWithText(text(R.string.activity_conflict)).assertExists()
        compose.onNodeWithContentDescription(text(R.string.activity_back)).performClick()
        compose.onNodeWithText(text(R.string.activity_save)).performClick()
        compose.runOnIdle { assertEquals(540, saved?.begin) }
    }

    @Test fun validMoveAndBothHandlesUpdateSavedTime() {
        var saved: ActivityDraft? = null
        val activity = TimetableActivity(17, "Study", "", 0, 540, 600)
        setContent { Theme { ActivityCreationView(Timetable("12", emptyList()), "Spring", activity,
            onClose = {}, onSave = { saved = it }, onRefresh = {}) } }
        compose.onNodeWithText(text(R.string.activity_adjust)).performScrollTo().performClick()
        val indicator = compose.onNode(hasContentDescription("Study", substring = true))
        val originalHeight = indicator.fetchSemanticsNode().boundsInRoot.height
        indicator.performTouchInput {
            down(center); advanceEventTime(700)
            moveBy(Offset(width * 1.1f, height * .4f)); up()
        }
        compose.onNodeWithContentDescription(text(R.string.activity_end_handle)).performTouchInput {
            down(center); moveBy(Offset(0f, originalHeight * .5f)); up()
        }
        compose.onNodeWithContentDescription(text(R.string.activity_start_handle)).performTouchInput {
            down(center); moveBy(Offset(0f, originalHeight * .25f)); up()
        }
        screenshot("activity-adjusted")
        compose.onNodeWithContentDescription(text(R.string.activity_back)).performClick()
        compose.onNodeWithText(text(R.string.activity_save)).performClick()
        compose.runOnIdle {
            assertEquals(1, saved?.day)
            assertEquals(585, saved?.begin)
            assertEquals(660, saved?.end)
        }
    }

    @Test fun savedActivityShowsDetailsAndEditActionWithoutOpeningLecture() {
        val activity = TimetableActivity(17, "Study group", "Library", 0, 540, 600)
        var edited: TimetableActivity? = null
        var lectureOpened = false
        setContent { Theme {
            Box(Modifier.fillMaxWidth().height(500.dp)) {
                TimetableGrid(
                    PreviewTimetableViewModel(Timetable("12", emptyList(), listOf(activity))),
                    onLectureSelected = { lectureOpened = true }, showDeleteDialog = {}, onEditActivity = { edited = it })
            }
        } }
        compose.onNodeWithText("Study group").performClick()
        compose.onNode(isDialog()).assertExists()
        compose.onAllNodesWithText("Library").assertCountEquals(2)
        compose.onNodeWithText(text(R.string.activity_close)).performClick()
        compose.onNodeWithText("Study group").performTouchInput { longClick() }
        compose.onNodeWithText(text(R.string.activity_edit)).performClick()
        compose.runOnIdle { assertEquals(activity, edited); assertFalse(lectureOpened) }
    }

    @Test fun activityListAppearsBelowLecturesAndOpensDetails() {
        val activity = TimetableActivity(17, "Study group", "Library", 0, 540, 600)
        val model = PreviewTimetableViewModel(Timetable("12", emptyList(), listOf(activity)))
        setContent { Theme {
            TimetableView(model, rememberNavController())
        } }
        val count = InstrumentationRegistry.getInstrumentation().targetContext.resources.getQuantityString(R.plurals.activities_count, 1, 1)
        compose.onNodeWithText(count).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(text(R.string.no_lecture_for_timetable)).assertExists()
        val options = compose.onNodeWithContentDescription(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.activity_options, activity.title))
        options.performScrollTo()
        screenshot("activity-list")
        options.performClick()
        compose.onNodeWithText(text(R.string.activity_edit)).assertExists()
        compose.onNodeWithText(text(R.string.activity_delete)).assertExists()
    }

}
