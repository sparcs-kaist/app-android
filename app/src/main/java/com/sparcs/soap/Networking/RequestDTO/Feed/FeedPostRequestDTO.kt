package com.sparcs.soap.Networking.RequestDTO.Feed

import com.sparcs.soap.Domain.Models.Feed.FeedCreatePost
import com.google.gson.annotations.SerializedName

data class FeedPostRequestDTO (
    @SerializedName("content")
    val content: String,

    @SerializedName("is_anonymous")
    val isAnonymous: Boolean,

    @SerializedName("image_ids")
    val imageIDs: List<String>
) {
    companion object {
        fun fromModel(model: FeedCreatePost): FeedPostRequestDTO {
            return FeedPostRequestDTO(
                content = model.content,
                isAnonymous = model.isAnonymous,
                imageIDs = model.images.map { it.id }
            )
        }
    }
}