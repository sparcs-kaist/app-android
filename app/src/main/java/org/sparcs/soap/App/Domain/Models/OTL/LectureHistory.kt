package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType

data class LectureHistory(
    val year: Int,
    val semester: SemesterType,
    val lectures: List<ReducedLecture>
)

data class ReducedLecture(
    val lectureId: Int,
    val written: Boolean
)