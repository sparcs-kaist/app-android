package org.sparcs.soap.App.Networking.ResponseDTO.Feed

import com.google.gson.annotations.SerializedName


data class FeedProfileImageDTO(
    @SerializedName("id") val id: String,
    @SerializedName("s3_key") val s3Key: String,
    @SerializedName("url") val url: String? = null
)