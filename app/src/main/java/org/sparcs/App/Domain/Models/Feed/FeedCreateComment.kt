package org.sparcs.App.Domain.Models.Feed

data class FeedCreateComment(
    val content: String,
    val isAnonymous: Boolean,
    val image: FeedImage?
)