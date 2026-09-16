package org.sparcs.soap.timetableTests

import android.app.Application
import androidx.datastore.preferences.core.preferencesOf
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.helpers.TimetableThemeStore
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.LectureClass
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableActivity
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.wearable.WatchTimetable
import org.sparcs.soap.wearable.themed
import org.sparcs.soap.wearable.toWatchModel
import org.sparcs.soap.widgets.buddyTimetableWidget.WidgetTimetableEntry
import org.sparcs.soap.widgets.buddyTimetableWidget.toWidgetUiState
import org.sparcs.soap.widgets.buddyUpcomingClassWidget.WidgetLectureEntry
import org.sparcs.soap.widgets.WIDGET_THEME_ID
import org.sparcs.soap.widgets.themed
import org.sparcs.soap.widgets.toWidgetHex

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class TimetableThemeSurfacesTest {
    private val theme = TimetableTheme.builtIn.first { it.id == "builtin.ocean" }
    private val activity = TimetableActivity(17, "Study", "Library", 0, 600, 660)
    private val lecture = Lecture.mockList().first()
        .copy(courseID = 3, classes = listOf(LectureClass(DayType.MON, 540, 600, "E11", "Building", "101")))
    private val table = Timetable("12", listOf(lecture), listOf(activity))

    @Test fun `widget entries carry the selected palette and the slot that produced it`() {
        val state = table.toWidgetUiState(theme).timetable!!
        val (lectureEntry, activityEntry) = state.getEntries(DayType.MON)
        assertEquals(theme.colorFor(3).toWidgetHex(), lectureEntry.bgColor)
        assertEquals(theme.textColor.toWidgetHex(), lectureEntry.textColor)
        assertEquals(3, lectureEntry.colorID)
        assertEquals(theme.colorFor(17).toWidgetHex(), activityEntry.bgColor)
        assertEquals(17, activityEntry.colorID)
    }

    @Test fun `widget state recolors on a theme change without another sync`() {
        val synced = table.toWidgetUiState(TimetableTheme.Default).timetable!!
        val recolored = synced.themed(theme)
        assertEquals(theme.colorFor(3).toWidgetHex(), recolored.getLectures(DayType.MON).single().bgColor)
        assertEquals(theme.colorFor(17).toWidgetHex(), recolored.activitiesByDay.getValue(DayType.MON).single().bgColor)
        assertEquals(theme.textColor.toWidgetHex(), recolored.getLectures(DayType.MON).single().textColor)
    }

    @Test fun `widget entries synced before themes keep their baked colors`() {
        val old = Json.decodeFromString<WidgetTimetableEntry>(
            """{"lecturesByDay":{"MON":[{"title":"Old","classroom":"101","day":"MON","signInRequired":false,""" +
                """"startMinutes":540,"durationMinutes":60,"bgColor":"#4A90E2","textColor":"#FFFFFF"}]},""" +
                """"visibleDays":["MON"],"minMinutes":540,"maxMinutes":1080}"""
        )
        val entry = old.themed(theme).getLectures(DayType.MON).single()
        assertNull(entry.colorID)
        assertEquals("#4A90E2", entry.bgColor)
        assertEquals("#FFFFFF", entry.textColor)
    }

    @Test fun `up next entry follows the theme`() {
        val entry = WidgetLectureEntry(
            title = "Next", classroom = "101", day = DayType.MON, signInRequired = false,
            startMinutes = 540, durationMinutes = 60, colorID = 5
        ).themed(theme)
        assertEquals(theme.colorFor(5).toWidgetHex(), entry.bgColor)
        assertEquals(theme.textColor.toWidgetHex(), entry.textColor)
    }

    @Test fun `watch payload is themed and can be recolored from its own copy`() {
        val payload = table.toWatchModel(theme)
        val watchLecture = payload.lectures.single()
        assertEquals(theme.colorFor(3).toWidgetHex(), watchLecture.color)
        assertEquals(3, watchLecture.colorID)

        val other = TimetableTheme.builtIn.first { it.id == "builtin.sunset" }
        assertEquals(other.colorFor(3).toWidgetHex(), payload.themed(other).lectures.single().color)
    }

    @Test fun `a widget keeps the theme picked in its configuration`() {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("timetable_themes", 0).edit().clear().commit()
        TimetableThemeStore(context).select("builtin.sunset")
        val state = TimetableThemeStore(context).state

        val configured = preferencesOf(WIDGET_THEME_ID to theme.id)
        assertEquals(theme.id, state.theme(configured[WIDGET_THEME_ID]).id)

        // An unconfigured widget, or one pointing at a deleted theme, shows what the app shows.
        assertEquals("builtin.sunset", state.theme(preferencesOf()[WIDGET_THEME_ID]).id)
        assertEquals("builtin.sunset", state.theme("custom.deleted").id)
    }

    @Test fun `watch payloads sent before themes keep their colors`() {
        val old = Json { ignoreUnknownKeys = true }.decodeFromString<WatchTimetable>(
            """{"id":"12","lectures":[{"id":1,"name":"Old","code":"CS101","classes":[],"color":"#4A90E2"}]}"""
        )
        assertEquals("#4A90E2", old.themed(theme).lectures.single().color)
    }
}
