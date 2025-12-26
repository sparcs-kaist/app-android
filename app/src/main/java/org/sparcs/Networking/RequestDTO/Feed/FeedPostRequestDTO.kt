package org.sparcs.Networking.RequestDTO.Feed

import com.google.gson.annotations.SerializedName
import org.sparcs.Domain.Models.Feed.FeedCreatePost

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