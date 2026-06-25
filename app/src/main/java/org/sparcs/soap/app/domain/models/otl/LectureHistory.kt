package org.sparcs.soap.app.domain.models.otl

import org.sparcs.soap.app.domain.enums.otl.SemesterType

data class LectureHistory(
    val year: Int,
    val semester: SemesterType,
    val lectures: List<ReducedLecture>
)

data class ReducedLecture(
    val lectureId: Int,
    val written: Boolean
)