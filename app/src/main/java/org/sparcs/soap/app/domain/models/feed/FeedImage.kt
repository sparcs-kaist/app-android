package org.sparcs.soap.app.domain.models.feed

data class FeedImage(
    val id: String,
    val url: String,
    val mimeType: String,
    val size: Int,
    val spoiler: Boolean?
)