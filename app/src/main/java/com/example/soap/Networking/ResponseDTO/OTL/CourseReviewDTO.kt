package com.example.soap.Networking.ResponseDTO.OTL

import com.example.soap.Domain.Models.OTL.CourseReview
import kotlinx.serialization.SerialName

data class CourseReviewDTO(
    @SerialName("id")
    val id: Int,

    @SerialName("content")
    val content: String,

    @SerialName("lecture")
    val lecture: LectureDTO,

    @SerialName("grade")
    val grade: Int,

    @SerialName("like")
    val like: Int,

    @SerialName("load")
    val load: Int,

    @SerialName("speech")
    val speech: Int,

    @SerialName("is_deleted")
    val isDeleted: Int,

    @SerialName("userspecific_is_liked")
    val isLiked: Boolean
) {
    fun toModel(): CourseReview {
        val lectureModel = lecture.toModel()

        return CourseReview(
            id = id,
            content = content,
            professor = lectureModel.professors.firstOrNull()?.name,
            year = lectureModel.year,
            semester = lectureModel.semester,
            grade = grade,
            like = like,
            load = load,
            speech = speech,
            isDeleted = isDeleted != 0,
            isLiked = isLiked
        )
    }
}