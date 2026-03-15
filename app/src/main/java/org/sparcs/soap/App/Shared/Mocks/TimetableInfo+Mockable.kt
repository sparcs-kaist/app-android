package org.sparcs.soap.App.Shared.Mocks

import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Models.OTL.TimetableListItem

fun TimetableListItem.Companion.mock(): TimetableListItem {
    return TimetableListItem(
        id = 0,
        name = "Mock Timetable",
        year = 2026,
        semester = SemesterType.SPRING,
        timetableOrder = 0
    )
}

fun Timetable.Companion.mock(): Timetable {
    return Timetable(
        lectures = Lecture.mockList().take(3)
    )
}

fun Timetable.Companion.mockList(): List<Timetable> {
    return listOf(
        Timetable(
            lectures = Lecture.mockList().take(2)
        ),
        Timetable(
            lectures = Lecture.mockList().take(4)
        ),
        Timetable(
            lectures = Lecture.mockList().take(1)
        ),
        Timetable(
            lectures = Lecture.mockList().take(5)
        )
    )
}