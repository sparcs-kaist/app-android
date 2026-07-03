package org.sparcs.soap.app.shared.mocks.otl

import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.Timetable

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