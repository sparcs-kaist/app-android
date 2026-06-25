package org.sparcs.soap.app.domain.models.feed

data class FeedCreateComment(
    val content: String,
    val isAnonymous: Boolean,
    val image: FeedImage?
)