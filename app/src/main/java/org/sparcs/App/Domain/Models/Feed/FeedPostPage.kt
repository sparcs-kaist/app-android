package org.sparcs.App.Domain.Models.Feed

data class FeedPostPage(
    val items: List<FeedPost>,
    val nextCursor: String?,
    val hasNext: Boolean
)