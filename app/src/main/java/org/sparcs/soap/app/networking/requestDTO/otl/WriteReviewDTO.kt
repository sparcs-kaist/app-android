package org.sparcs.soap.app.networking.requestDTO.otl

import com.google.gson.annotations.SerializedName

data class WriteReviewRequest(
    @SerializedName("lectureId")
    val lectureID: Int,

    @SerializedName("content")
    val content: String,

    @SerializedName("grade")
    val grade: Int,

    @SerializedName("load")
    val load: Int,

    @SerializedName("speech")
    val speech: Int
)

data class EditReviewRequest(
    @SerializedName("content")
    val content: String,

    @SerializedName("grade")
    val grade: Int,

    @SerializedName("load")
    val load: Int,

    @SerializedName("speech")
    val speech: Int
)