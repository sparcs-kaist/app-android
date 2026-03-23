package org.sparcs.soap.App.Shared.Mocks.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType
import org.sparcs.soap.App.Domain.Models.OTL.CourseHistory
import org.sparcs.soap.App.Domain.Models.OTL.CourseHistoryClass

fun CourseHistory.Companion.mock() = CourseHistory(
    year = 2026,
    semester = SemesterType.SPRING,
    classes = CourseHistoryClass.mockList(),
    myLectureID = 12345
)

fun CourseHistory.Companion.mockList() = listOf(
    CourseHistory.mock(),
    CourseHistory.mock().copy(
        year = 2025,
        semester = SemesterType.AUTUMN,
        classes = CourseHistoryClass.mockList().take(2),
        myLectureID = null
    ),
    CourseHistory.mock().copy(
        year = 2025,
        semester = SemesterType.SPRING,
        classes = CourseHistoryClass.mockList(),
        myLectureID = 54321
    )
)