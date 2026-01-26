package org.sparcs.soap.App.Domain.Models.Feed

data class FeedImage(
    val id: String,
    val url: String,
    val mimeType: String,
    val size: Int,
    val spoiler: Boolean?
)