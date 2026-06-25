package org.sparcs.soap.app.networking.responseDTO.feed

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.feed.FeedPostPage

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