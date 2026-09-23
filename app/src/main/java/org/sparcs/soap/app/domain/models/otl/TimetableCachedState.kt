package org.sparcs.soap.app.domain.models.otl

import java.util.Date

data class TimetableCachedState(
    val semesters: List<Semester>? = null,
    val currentSemester: Semester? = null,
    val timetables: List<TimetableSummary>? = null,
    val timetable: Timetable? = null,
    val updatedAt: Date? = null,
)
