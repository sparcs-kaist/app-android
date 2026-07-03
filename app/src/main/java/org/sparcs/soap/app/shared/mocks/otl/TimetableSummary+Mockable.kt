package org.sparcs.soap.app.shared.mocks.otl

import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.models.otl.TimetableSummary

fun TimetableSummary.Companion.mock() = TimetableSummary(
    id = 1,
    title = "나의 시간표",
    year = 2026,
    semester = SemesterType.SPRING
)

fun TimetableSummary.Companion.mockList() = listOf(
    TimetableSummary(id = 1, title = "주전공 시간표", year = 2026, semester = SemesterType.SPRING),
    TimetableSummary(id = 2, title = "복전 탐색용", year = 2026, semester = SemesterType.SPRING),
    TimetableSummary(id = 3, title = "2025 가을학기", year = 2025, semester = SemesterType.AUTUMN)
)