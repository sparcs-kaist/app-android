package org.sparcs.soap.timetableTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.LectureItem
import org.sparcs.soap.app.features.timetable.components.roomLabel
import org.sparcs.soap.app.features.timetable.components.roomNumber
import org.sparcs.soap.app.shared.mocks.otl.mockList

class TimetableGridCellTest {
    private val lectureClass = LectureItem.mockList()[1].lectureClass
        .copy(buildingCode = "E3-1", roomName = "(1501호)강의실-제1공동강의실")

    @Test
    fun `room number is taken from the leading parenthesis`() {
        assertEquals("1501", roomNumber("(1501호)강의실-제1공동강의실"))
    }

    @Test
    fun `spaces around the number are ignored`() {
        assertEquals("201", roomNumber(" ( 201 호 ) 세미나실"))
    }

    @Test
    fun `a room without a numbered parenthesis has no room number`() {
        assertNull(roomNumber("터만홀"))
        assertNull(roomNumber("강의실(1501호)"))
    }

    @Test
    fun `packed cells keep only the room number beside the building`() {
        assertEquals("(E3-1) (1501호)강의실-제1공동강의실", lectureClass.roomLabel(isPacked = false))
        assertEquals("(E3-1) 1501", lectureClass.roomLabel(isPacked = true))
    }
}
