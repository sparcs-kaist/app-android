package com.example.soap.Domain.Models.Ara

import java.net.URL
import java.util.Date
import java.util.UUID

data class Post(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val description: String,
    val voteCount: Int,
    val commentCount: Int,
    val author: String,
    val createdAt: Date,
    val thumbnailURL: URL?,
) {
    companion object { }
}
