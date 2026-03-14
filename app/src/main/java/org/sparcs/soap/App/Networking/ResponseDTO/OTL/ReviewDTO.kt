package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType
import org.sparcs.soap.App.Domain.Models.OTL.Review
import org.sparcs.soap.App.Domain.Models.OTL.ReviewResponse

data class ReviewDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("courseId")
    val courseId: Int,

    @SerializedName("lectureId")
    val lectureId: Int,

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
    fun toModel(): Review = Review(
        id = id,
        courseId = courseId,
        lectureId = lectureId,
        courseName = courseName,
        professors = professors.map { it.toModel() },
        year = year,
        semester = SemesterType.fromRawValue(semester),
        content = content,
        like = like,
        grade = grade,
        load = load,
        speech = speech,
        isDeleted = isDeleted,
        isLiked = likedByUser
    )
}

data class ReviewResponseDTO(
    @SerializedName("reviews")
    val reviews: List<ReviewDTO>,

    @SerializedName("averageGrade")
    val averageGrade: Double,

    @SerializedName("averageLoad")
    val averageLoad: Double,

    @SerializedName("averageSpeech")
    val averageSpeech: Double,

    @SerializedName("department")
    val department: DepartmentDTO?,

    @SerializedName("totalCount")
    val totalCount: Int
) {
    fun toModel(): ReviewResponse = ReviewResponse(
        reviews = reviews.map { it.toModel() },
        grade = averageGrade,
        load = averageLoad,
        speech = averageSpeech
    )
}

data class WrittenReviewResponseDTO(
    @SerializedName("reviews")
    val reviews: List<ReviewDTO>
)
