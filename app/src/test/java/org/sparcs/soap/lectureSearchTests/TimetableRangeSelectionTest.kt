package org.sparcs.soap.lectureSearchTests

import org.junit.Assert.assertEquals
import org.junit.Test
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.LectureTimeFilter
import org.sparcs.soap.app.domain.models.otl.TimetableRangeSelection as Selection

class TimetableRangeSelectionTest {
    @Test fun tapChoosesOneHalfHour() {
        assertEquals(LectureTimeFilter(DayType.MON, 540, 570), Selection.range(0.1f, 65f / 900, 65f / 900))
    }
    @Test fun upwardDragIncludesBothTouchedCells() {
        assertEquals(LectureTimeFilter(DayType.WED, 540, 660), Selection.range(0.5f, 155f / 900, 65f / 900))
    }
    @Test fun dragClampsToWeekdaysAndSelectableHours() {
        assertEquals(LectureTimeFilter(DayType.FRI, 480, 1380), Selection.range(2f, -1f, 2f))
        assertEquals(LectureTimeFilter(DayType.MON, 1350, 1380), Selection.range(-1f, 1f, 1f))
    }
    @Test fun movingAtBoundsPreservesDuration() {
        val block = LectureTimeFilter(DayType.MON, 480, 600)
        assertEquals(block, Selection.move(block, -30))
        assertEquals(block.copy(begin = 1260, end = 1380), Selection.move(block, 2000))
    }
    @Test fun accessibilityDefaultsAndDayClamping() {
        assertEquals(LectureTimeFilter(DayType.MON, 540, 600), Selection.normalized(LectureTimeFilter()))
        assertEquals(DayType.MON, Selection.moveDay(LectureTimeFilter(), -1).day)
        assertEquals(DayType.TUE, Selection.moveDay(LectureTimeFilter(), 1).day)
    }
    @Test fun extendingAtDayEndWrapsToHalfHour() {
        assertEquals(LectureTimeFilter(DayType.FRI, 1200, 1230), Selection.extend(LectureTimeFilter(DayType.FRI, 1200, 1380)))
    }
}
