package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.OTL.LectureReview

data class LectureReviewDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("lecture")
    val lecture: LectureDTO,

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

    @SerializedName("is_deleted")
    val isDeleted: Int,

    @SerializedName("userspecific_is_liked")
    val isLiked: Boolean
) {
    fun toModel(): LectureReview = LectureReview(
        id = id,
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