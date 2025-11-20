package com.sparcs.soap.Networking.RequestDTO.OTL

import com.google.gson.annotations.SerializedName

data class WriteReviewRequest(
    @SerializedName("content")
    val content: String,

    @SerializedName("grade")
    val grade: Int,

    @SerializedName("load")
    val load: Int,

    @SerializedName("speech")
    val speech: Int
)