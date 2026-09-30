package org.sparcs.soap.app.networking.responseDTO.otl

import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.models.otl.OTLTakenLecture
import org.sparcs.soap.app.domain.models.otl.OTLUserLectureHistory
import org.sparcs.soap.app.domain.models.otl.OTLUserLectureSemester

data class OTLUserLectureHistoryDTO(
    val lecturesWrap: List<OTLUserLectureSemesterDTO>,
    val reviewedLecturesCount: Int,
    val totalLecturesCount: Int,
    val totalLikesCount: Int,
) {
    fun toModel() = OTLUserLectureHistory(
        lecturesWrap.map { it.toModel() }, totalLecturesCount, reviewedLecturesCount, totalLikesCount
    )
}

data class OTLUserLectureSemesterDTO(val year: Int, val semester: Int, val lectures: List<OTLTakenLectureDTO>) {
    fun toModel() = OTLUserLectureSemester(year, SemesterType.fromRawValue(semester), lectures.map { it.toModel() })
}

data class OTLTakenLectureDTO(
    val courseId: Int,
    val lectureId: Int,
    val name: String,
    val code: String,
    val professors: List<ProfessorDTO>,
    val written: Boolean,
) {
    fun toModel() = OTLTakenLecture(courseId, lectureId, name, code, professors.map { it.toModel() }, written)
}
