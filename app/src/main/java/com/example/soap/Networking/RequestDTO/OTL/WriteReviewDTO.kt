package com.example.soap.Networking.RequestDTO.OTL

import kotlinx.serialization.SerialName

data class WriteReviewRequest(
    @SerialName("lecture")
    val lecture: Int,

    @SerialName("content")
    val content: String,

    @SerialName("grade")
    val grade: Int,

    @SerialName("load")
    val load: Int,

    @SerialName("speech")
    val speech: Int
)