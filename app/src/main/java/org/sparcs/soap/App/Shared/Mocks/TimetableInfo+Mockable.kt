package org.sparcs.soap.App.Shared.Mocks

import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.Timetable


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