package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType
import org.sparcs.soap.App.Domain.Models.OTL.LectureReview

data class LectureReviewDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("courseId")
    val courseID: Int,

    @SerializedName("lectureId")
    val lectureID: Int,

    @SerializedName("courseName")
    val courseName: String,

    @SerializedName("professors")
    val professors: List<ProfessorDTO>,

    @SerializedName("year")
    val year: Int,

    @SerializedName("semester")
    val semester: Int,

    @SerializedName("content")
    val content: String,

    @SerializedName("like")
    val like: Int,

    @SerializedName("grade")
    val grade: Int,

    @SerializedName("load")
    val load: Int,

    @SerializedName("speech")
    val speech: Int,

    @SerializedName("isDeleted")
    val isDeleted: Boolean,

    @SerializedName("likedByUser")
    val likedByUser: Boolean
) {
    fun toModel(): LectureReview = LectureReview(
        id = id,
        courseID = courseID,
        lectureID = lectureID,
        courseName = courseName,
        professors = professors.map { it.toModel() },
        year = year,
        semester = SemesterType.fromRawValue(semester),
        content = content,
        like = like,
        grade = ratingToString(grade),
        load = ratingToString(load),
        speech = ratingToString(speech),
        isDeleted = isDeleted,
        likedByUser = likedByUser
    )
}

fun ratingToString(rating: Int): String = when (rating) {
    1 -> "F"
    2 -> "D"
    3 -> "C"
    4 -> "B"
    5 -> "A"
    else -> "?"
}

data class WrittenReviewResponseDTO(
    @SerializedName("reviews")
    val reviews: List<LectureReviewDTO>
)
