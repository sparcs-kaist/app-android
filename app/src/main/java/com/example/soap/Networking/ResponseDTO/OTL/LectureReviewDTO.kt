package com.example.soap.Networking.ResponseDTO.OTL

import com.example.soap.Domain.Models.OTL.LectureReview
import kotlinx.serialization.SerialName

data class LectureReviewDTO(
    @SerialName("id")
    val id: Int,

    @SerialName("course")
    val course: CourseDTO,

    @SerialName("lecture")
    val lecture: LectureDTO,

    @SerialName("content")
    val content: String,

    @SerialName("like")
    val like: Int,

    @SerialName("grade")
    val grade: Int,

    @SerialName("load")
    val load: Int,

    @SerialName("speech")
    val speech: Int,

    @SerialName("is_deleted")
    val isDeleted: Int,

    @SerialName("userspecific_is_liked")
    val isLiked: Boolean
) {
    fun toModel(): LectureReview = LectureReview(
        id = id,
        course = course.toModel(),
        lecture = lecture.toModel(),
        content = content,
        like = like,
        grade = grade,
        load = load,
        speech = speech,
        isDeleted = isDeleted != 0,
        isLiked = isLiked
    )
}