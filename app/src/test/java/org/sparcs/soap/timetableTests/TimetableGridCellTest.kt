package org.sparcs.soap.timetableTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.LectureItem
import org.sparcs.soap.app.domain.models.otl.Professor
import org.sparcs.soap.app.features.timetable.components.TimetablePlacement
import org.sparcs.soap.app.features.timetable.components.detailText
import org.sparcs.soap.app.features.timetable.components.roomNumber
import org.sparcs.soap.app.shared.mocks.otl.mockList

class TimetableGridCellTest {
    private val item = LectureItem.mockList()[1].let {
        it.copy(lectureClass = it.lectureClass.copy(buildingCode = "E3-1", roomName = "(1501호)강의실-제1공동강의실"))
    }

    private fun withProfessors(vararg names: String) =
        item.copy(lecture = item.lecture.copy(professors = names.mapIndexed { index, name -> Professor(index, name) }))

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
    fun `shared image shows the first professor instead of the room`() {
        assertEquals("Professor Only", withProfessors("Professor Only", "Second").detailText(TimetablePlacement.Render, isPacked = false))
    }

    @Test
    fun `shared image falls back to the room without a professor`() {
        assertEquals("(E3-1) (1501호)강의실-제1공동강의실", withProfessors().detailText(TimetablePlacement.Render, isPacked = false))
        assertEquals("(E3-1) (1501호)강의실-제1공동강의실", withProfessors(" ").detailText(TimetablePlacement.Render, isPacked = false))
    }

    @Test
    fun `the timetable shows the room and packed cells keep only its number`() {
        val lecture = withProfessors("Professor Only")
        assertEquals("(E3-1) (1501호)강의실-제1공동강의실", lecture.detailText(TimetablePlacement.View, isPacked = false))
        assertEquals("(E3-1) 1501", lecture.detailText(TimetablePlacement.View, isPacked = true))
    }
}
