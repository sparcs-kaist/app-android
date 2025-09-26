package com.example.soap.Domain.Models.Feed

import java.net.URL

data class FeedUser(
    val id: String,
    val nickname: String,
    val profileImageURL: URL?,
    val karma: Int
)