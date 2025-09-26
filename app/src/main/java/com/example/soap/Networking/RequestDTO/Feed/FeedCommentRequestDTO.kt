package com.example.soap.Networking.RequestDTO.Feed

import com.example.soap.Domain.Models.Feed.FeedCreateComment
import com.google.gson.annotations.SerializedName

data class FeedCommentRequestDTO (
    @SerializedName("content")
    val content: String,

    @SerializedName("is_anonymous")
    val isAnonymous: Boolean,

    @SerializedName("image_id")
    val imageID: FeedImageRequestDTO?
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