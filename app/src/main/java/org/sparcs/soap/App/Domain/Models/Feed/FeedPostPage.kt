package org.sparcs.soap.App.Domain.Models.Feed

data class FeedPostPage(
    val items: List<FeedPost>,
    val nextCursor: String?,
    val hasNext: Boolean
)