package org.sparcs.soap.app.domain.models.feed

data class FeedPostPage(
    val items: List<FeedPost>,
    val nextCursor: String?,
    val hasNext: Boolean
)