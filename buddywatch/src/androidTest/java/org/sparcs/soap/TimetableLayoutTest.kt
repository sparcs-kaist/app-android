package org.sparcs.soap

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.sparcs.soap.data.models.Timetable
import org.sparcs.soap.data.models.TimetableActivity
import org.sparcs.soap.presentation.LectureViewOption
import org.sparcs.soap.presentation.theme.SoapTheme
import org.sparcs.soap.presentation.ui.DayTimetableView
import org.sparcs.soap.presentation.ui.LectureRootView
import org.sparcs.soap.presentation.ui.WeekTimetableView
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class TimetableLayoutTest {
    @get:Rule
    val compose = createComposeRule()

    private val now = LocalDateTime.of(2026, 9, 22, 11, 0)

    @Test
    fun weekKeepsDaysVisibleWhileScrolling() {
        compose.setContent {
            SoapTheme { WeekTimetableView(Timetable.mock(), now.dayOfWeek, now, {}) {} }
        }
        val monday = DayOfWeek.MONDAY.getDisplayName(TextStyle.NARROW_STANDALONE, Locale.getDefault())
        val top = compose.onNodeWithText(monday).fetchSemanticsNode().boundsInRoot.top
        screenshot("week")
        compose.onRoot().performTouchInput { swipeUp() }
        compose.onNodeWithText(monday).assertIsDisplayed()
        assertEquals(top, compose.onNodeWithText(monday).fetchSemanticsNode().boundsInRoot.top)
        screenshot("week-scrolled")
    }

    @Test
    fun dayEntryOpensDetailAndBackReturnsToDay() {
        compose.setContent {
            SoapTheme { LectureRootView(Timetable.mock(), LectureViewOption.DAY, now) {} }
        }
        screenshot("day")
        val entry = Timetable.mock().lectures.first { it.id == 1928379 }
        compose.onNodeWithContentDescription(entry.name, substring = true).performClick()
        compose.onNodeWithText(entry.name).assertIsDisplayed()
        screenshot("detail")
        compose.onRoot().performTouchInput { swipeRight() }
        compose.onNodeWithContentDescription(entry.name, substring = true).assertIsDisplayed()
    }

    @Test
    fun overlappingAndShortActivitiesStayInTheirLanes() {
        val timetable = Timetable.mock(activities = listOf(
            TimetableActivity(10, "Project meeting", "TUE", 660, 720, "E3", "#845EF7"),
            TimetableActivity(11, "Check-in", "TUE", 720, 735, "Library", "#20C997")
        ))
        compose.setContent {
            SoapTheme { DayTimetableView(timetable, now.dayOfWeek, now, {}) {} }
        }
        screenshot("overlap")
        val lecture = timetable.lectures.first { it.id == 1928379 }
        val lectureBounds = compose.onNodeWithContentDescription(lecture.name, substring = true).fetchSemanticsNode().boundsInRoot
        val meetingBounds = compose.onNodeWithContentDescription("Project meeting", substring = true).fetchSemanticsNode().boundsInRoot
        assertTrue(lectureBounds.right <= meetingBounds.left)
        assertEquals(lectureBounds.bottom, meetingBounds.bottom)
        compose.onNodeWithContentDescription("Check-in", substring = true).performScrollTo().assertIsDisplayed()
    }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        File(context.getExternalFilesDir(null), "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
