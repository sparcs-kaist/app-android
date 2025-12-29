package org.sparcs.App.Networking.ResponseDTO.Feed

import com.google.gson.annotations.SerializedName
import org.sparcs.App.Domain.Models.Feed.FeedUser

data class FeedUserDTO (
    @SerializedName("id")
    val id: String,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("profile_image_url")
    val profileImageURL: String?,

    @SerializedName("karma_total")
    val karma: Int
) {
    fun toModel(): FeedUser {
        return FeedUser(
            id = id,
            nickname = nickname,
            profileImageURL = profileImageURL,
            karma = karma
        )
    }
}