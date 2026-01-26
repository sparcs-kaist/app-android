package org.sparcs.soap.App.Domain.Models.Feed

data class FeedCreatePost(
    val content: String,
    val isAnonymous: Boolean,
    val images: List<FeedImage>
)