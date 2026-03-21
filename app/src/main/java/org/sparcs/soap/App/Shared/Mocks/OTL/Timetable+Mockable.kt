package org.sparcs.soap.App.Shared.Mocks.OTL

import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.Timetable

fun Timetable.Companion.mock(): Timetable {
    return Timetable(
        id = "0",
        lectures = Lecture.mockList().take(3)
    )
}

fun Timetable.Companion.mockList(): List<Timetable> {
    return listOf(
        Timetable(
            id = "0",
            lectures = Lecture.mockList().take(2)
        ),
        Timetable(
            id = "1",
            lectures = Lecture.mockList().take(4)
        ),
        Timetable(
            id = "2",
            lectures = Lecture.mockList().take(1)
        )
    )
}