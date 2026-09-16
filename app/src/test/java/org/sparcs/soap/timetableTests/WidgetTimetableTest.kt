package org.sparcs.soap.timetableTests

import android.app.Application
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.*
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.widgets.buddyTimetableWidget.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class WidgetTimetableTest {
    private val activity = TimetableActivity(17, "Study", "Library", 0, 600, 660)

    @Test fun activityUsesItsOwnIdentityColorAndLocation() {
        val state = Timetable("12", emptyList(), listOf(activity)).toWidgetUiState().timetable!!
        val entry = state.getEntries(DayType.MON).single()
        assertEquals(17, entry.activityID)
        assertEquals("Study", entry.title)
        assertEquals("Library", entry.classroom)
        assertEquals(600, entry.startMinutes)
        assertEquals(60, entry.durationMinutes)
        assertEquals(String.format("#%06X", 0xFFFFFF and activity.backgroundColor.toArgb()), entry.bgColor)
        assertTrue(state.getLectures(DayType.MON).isEmpty())
    }
    @Test fun classesAndActivitiesShareOneTimeScale() {
        val lecture = Lecture.mockList().first().copy(classes = listOf(LectureClass(DayType.MON, 540, 600, "E11", "Building", "101")))
        val state = Timetable("12", listOf(lecture), listOf(activity)).toWidgetUiState().timetable!!
        assertEquals(listOf(null, 17), state.getEntries(DayType.MON).map { it.activityID })
        assertEquals(540, state.minMinutes)
        assertEquals(660, state.maxMinutes)
    }
    @Test fun weekendAndMidnightActivityAreVisibleWithoutAnExtraDay() {
        val state = Timetable("12", emptyList(), listOf(activity.copy(day = 6, begin = 1380, end = 1440))).toWidgetUiState().timetable!!
        assertEquals(DayType.weekdays() + DayType.SUN, state.visibleDays)
        assertEquals(1380, state.minMinutes)
        assertEquals(1440, state.maxMinutes)
        assertEquals(1, state.getEntries(DayType.SUN).size)
    }
    @Test fun emptyAndOldWidgetStateRemainReadable() {
        val empty = Timetable("myTable", emptyList()).toWidgetUiState()
        assertEquals(540, empty.timetable!!.minMinutes)
        assertEquals(1080, empty.timetable.maxMinutes)
        val old = Json.decodeFromString<WidgetTimetableEntry>("""{"lecturesByDay":{},"visibleDays":["MON"],"minMinutes":540,"maxMinutes":1080}""")
        assertTrue(old.activitiesByDay.isEmpty())
        val state = Timetable("12", emptyList(), listOf(activity)).toWidgetUiState()
        assertEquals(state, Json.decodeFromString<TimetableUiState>(Json.encodeToString(state)))
    }
    @Test fun editsAndDeletesReplacePreviousWidgetEntries() {
        val edited = Timetable("12", emptyList(), listOf(activity.copy(title = "Edited", day = 1))).toWidgetUiState().timetable!!
        assertTrue(edited.getEntries(DayType.MON).isEmpty())
        assertEquals("Edited", edited.getEntries(DayType.TUE).single().title)
        assertTrue(Timetable("12", emptyList()).toWidgetUiState().timetable!!.activitiesByDay.isEmpty())
    }
    @Test fun syncingOneTableNeverMatchesMyTableOrAnotherTable() {
        val key = intPreferencesKey("selected_timetable_id")
        assertTrue(matchesSavedTimetable(preferencesOf(key to 12), "12"))
        assertFalse(matchesSavedTimetable(preferencesOf(key to 13), "12"))
        assertFalse(matchesSavedTimetable(preferencesOf(key to -1), "12"))
        assertFalse(matchesSavedTimetable(preferencesOf(), "12"))
        assertFalse(matchesSavedTimetable(preferencesOf(key to -1), "-1"))
    }
}
