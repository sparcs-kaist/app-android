package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType
import org.sparcs.soap.App.Domain.Models.OTL.LectureHistory
import org.sparcs.soap.App.Domain.Models.OTL.ReducedLecture

data class LectureWrapDTO(
    @SerializedName("year")
    val year: Int,

    @SerializedName("semester")
    val semester: Int,

    @SerializedName("lectures")
    val lectures: List<ShrinkedLectureDTO>
) {
    fun toModel(): LectureHistory = LectureHistory(
        year = year,
        semester = SemesterType.fromRawValue(semester),
        lectures = lectures.map { ReducedLecture(lectureId = it.lectureId, written = it.written) }
    )
}

data class ShrinkedLectureDTO(
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