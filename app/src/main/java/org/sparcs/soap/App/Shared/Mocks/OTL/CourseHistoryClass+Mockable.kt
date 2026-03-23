package org.sparcs.soap.App.Shared.Mocks.OTL

import org.sparcs.soap.App.Domain.Models.OTL.CourseHistoryClass

fun CourseHistoryClass.Companion.mock() = CourseHistoryClass(
    lectureID = 101,
    subtitle = "핵심 알고리즘",
    section = "A",
    professors = emptyList()
)

fun CourseHistoryClass.Companion.mockList() = listOf(
    CourseHistoryClass(
        lectureID = 101,
        subtitle = "핵심 알고리즘",
        section = "A",
        professors = emptyList()
    ),
    CourseHistoryClass(
        lectureID = 102,
        subtitle = "",
        section = "B",
        professors = emptyList()
    ),
    CourseHistoryClass(
        lectureID = 103,
        subtitle = "심화 주제",
        section = "C",
        professors = emptyList()
    )
)