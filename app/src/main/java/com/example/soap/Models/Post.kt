package com.example.soap.Models

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
    companion object {
        fun mocklist(): List<Post> {
            return listOf(
                Post(
                    title = "제목",
                    description = "얌얌",
                    voteCount = 120,
                    commentCount = 15,
                    author = "넙죽이",
                    createdAt = Date(),
                    thumbnailURL = null
                ),
                Post(
                    title = "개발자가 될테야",
                    description = "ㄱㄴㄷㄹ",
                    voteCount = 250,
                    commentCount = 30,
                    author = "밥",
                    createdAt = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24),
                    thumbnailURL = null
                ),
                Post(
                    title = "아기 거위",
                    description = "귀여움",
                    voteCount = 90,
                    commentCount = 8,
                    author = "거위",
                    createdAt = Date(),
                    thumbnailURL = null
                ),
                Post(
                    title = "집갈래",
                    description = "집좋아",
                    voteCount = 180,
                    commentCount = 22,
                    author = "너어업주기",
                    createdAt = Date(),
                    thumbnailURL = null
                )
            )
        }
    }
}