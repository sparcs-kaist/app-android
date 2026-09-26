package org.sparcs.soap.app.domain.models.otl

import org.sparcs.soap.app.domain.enums.otl.SemesterType

data class OTLUserLectureHistory(
    val semesters: List<OTLUserLectureSemester>,
    val totalLecturesCount: Int,
    val reviewedLecturesCount: Int,
    val totalLikesCount: Int,
)

data class OTLUserLectureSemester(
    val year: Int,
    val semesterType: SemesterType,
    val lectures: List<OTLTakenLecture>,
) {
    val id: String get() = "$year-$semesterType"
    val shortTitle: String get() = "${year.toString().takeLast(2)}${semesterType.shortCode}"
}

data class OTLTakenLecture(
    val courseID: Int,
    val lectureID: Int,
    val name: String,
    val code: String,
    val professors: List<Professor>,
    val hasWrittenReview: Boolean,
)
