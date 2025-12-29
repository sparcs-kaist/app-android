package org.sparcs.App.Networking.ResponseDTO.Feed

import com.google.gson.annotations.SerializedName
import org.sparcs.App.Domain.Models.Feed.FeedPostPage

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