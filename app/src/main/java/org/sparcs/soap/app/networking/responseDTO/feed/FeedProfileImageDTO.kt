package org.sparcs.soap.app.networking.responseDTO.feed

import com.google.gson.annotations.SerializedName


data class FeedProfileImageDTO(
    @SerializedName("profile_image_url") val url: String
)