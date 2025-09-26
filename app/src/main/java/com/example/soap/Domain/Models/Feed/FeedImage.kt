package com.example.soap.Domain.Models.Feed

import java.net.URL

data class FeedImage(
    val id: String,
    val url: URL,
    val mimeType: String,
    val size: Int,
    val spoiler: Boolean?
)