package com.example.soap.Shared.Mocks

import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Domain.Models.OTL.Timetable


fun Timetable.Companion.mock(): Timetable {
    return Timetable(
        id = "0",
        lectures = Lecture.mockList().shuffled().take((1..5).random())
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
        ),
        Timetable(
            id = "3",
            lectures = Lecture.mockList().take(5)
        )
    )
}