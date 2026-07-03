package org.sparcs.soap.app.domain.models.feed

data class FeedCreatePost(
    val content: String,
    val isAnonymous: Boolean,
    val images: List<FeedImage>
)