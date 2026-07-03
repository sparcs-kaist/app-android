package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.models.otl.LectureHistory
import org.sparcs.soap.app.domain.models.otl.ReducedLecture

data class LectureWrapDTO(
    @SerializedName("year")
    val year: Int,

    @SerializedName("semester")
    val semester: Int,

    @SerializedName("lectures")
    val lectures: List<ShrankLectureDTO>
) {
    fun toModel(): LectureHistory = LectureHistory(
        year = year,
        semester = SemesterType.fromRawValue(semester),
        lectures = lectures.map { ReducedLecture(lectureId = it.lectureId, written = it.written) }
    )
}

data class ShrankLectureDTO(
    @SerializedName("code")
    val code: String,

    @SerializedName("courseId")
    val courseId: Int,

    @SerializedName("lectureId")
    val lectureId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("professors")
    val professors: List<ProfessorDTO>,

    @SerializedName("written")
    val written: Boolean
)

data class LectureHistoryDTO(
    @SerializedName("lecturesWrap")
    val lecturesWrap: List<LectureWrapDTO>,

    @SerializedName("reviewedLecturesCount")
    val reviewedLecturesCount: Int,

    @SerializedName("totalLecturesCount")
    val totalLecturesCount: Int,

    @SerializedName("totalLikesCount")
    val totalLikesCount: Int
)

data class ReviewCreateResponseDTO(
    @SerializedName("reviewId")
    val reviewId: Int
)