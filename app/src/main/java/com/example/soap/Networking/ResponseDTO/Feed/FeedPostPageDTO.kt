package com.example.soap.Networking.ResponseDTO.Feed

import com.example.soap.Domain.Models.Feed.FeedPostPage
import com.google.gson.annotations.SerializedName

data class FeedPostPageDTO (
    @SerializedName("items")
    val items: List<FeedPostDTO>,

    @SerializedName("next_cursor")
    val nextCursor: String?,

    @SerializedName("has_next")
    val hasNext: Boolean
) {
    fun toModel(): FeedPostPage {
        return FeedPostPage(
            items = items.map { it.toModel() },
            nextCursor = nextCursor,
            hasNext = hasNext
        )
    }
}