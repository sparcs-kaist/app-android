package org.sparcs.soap.lectureSearchTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableActivity
import org.sparcs.soap.app.shared.mocks.otl.mock

class TimetableConflictTest {
    private val lecture = Lecture.mock().let { lecture ->
        lecture.copy(id = 1, classes = listOf(lecture.classes.first().copy(day = DayType.MON, begin = 600, end = 660)))
    }

    @Test
    fun `added lecture is not its own conflict`() {
        val table = Timetable("1", listOf(lecture))
        assertTrue(table.contains(lecture))
        assertFalse(table.hasCollision(lecture))
    }

    @Test
    fun `conflicts name lectures then activities and ignore touching endpoints`() {
        val overlapping = lecture.copy(id = 2, name = "Other")
        val adjacent = lecture.copy(id = 3, classes = listOf(lecture.classes.first().copy(begin = 660, end = 720)))
        val activity = TimetableActivity(1, "Meeting", "", 0, 630, 690)
        val table = Timetable("1", listOf(lecture, overlapping, adjacent), listOf(activity))
        assertEquals(listOf("Other", "Meeting"), table.conflicts(lecture))
    }
}
