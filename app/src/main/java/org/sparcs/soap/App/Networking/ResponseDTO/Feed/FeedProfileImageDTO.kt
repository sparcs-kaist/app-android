package org.sparcs.soap.App.Networking.ResponseDTO.Feed

import com.google.gson.annotations.SerializedName


data class FeedProfileImageDTO(
    @SerializedName("profile_image_url") val url: String
)