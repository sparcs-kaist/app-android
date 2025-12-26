package org.sparcs.Networking.RequestDTO.Feed

import com.google.gson.annotations.SerializedName
import org.sparcs.Domain.Models.Feed.FeedCreateComment

data class FeedCommentRequestDTO (
    @SerializedName("content")
    val content: String,

    @SerializedName("is_anonymous")
    val isAnonymous: Boolean,

    @SerializedName("image_id")
    val imageID: String?
) {
    companion object {
        fun fromModel(model: FeedCreateComment): FeedCommentRequestDTO {
            return FeedCommentRequestDTO(
                content = model.content,
                isAnonymous = model.isAnonymous,
                imageID = model.image?.id
            )
        }
    }
}