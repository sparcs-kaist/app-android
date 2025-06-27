package com.example.soap.Models

import java.net.URL
import java.time.Instant
import java.time.temporal.ChronoUnit
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
                    title = "some title",
                    description = "verrrry loooonnngggg description",
                    voteCount = 120,
                    commentCount = 15,
                    author = "ayayayayaya",
                    createdAt = Date(),
                    thumbnailURL = URL("https://d2phebdq64jyfk.cloudfront.net/media/article/8efac7c4150a4d67affcd29e509e9e78.png")
                ),
                Post(
                    title = "some title",
                    description = "verrrry loooonnngggg asdfojapsdofpoweufpoqiewfpoqiuwepfoiquwepfoiquwepfoiqwuepfoiqwuepfoiquwepfoiquwepofiquwepofiuqpwoeifuqpwoeifuqpwoeifu",
                    voteCount = -250,
                    commentCount = 30,
                    author = "goose",
                    createdAt = Date.from(Instant.now().minus(3, ChronoUnit.DAYS)),
                    thumbnailURL = null
                ),
                Post(
                    title = "mooooooooooooore tiiiiiiiiiitlellllllllllllllllllllllllllllllllll",
                    description = "verrrry loooonnngggg description",
                    voteCount = 90,
                    commentCount = 8,
                    author = "nupzuki",
                    createdAt = Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),
                    thumbnailURL = null
                ),
                Post(
                    title = "some title",
                    description = "less description",
                    voteCount = 180,
                    commentCount = 22,
                    author = "Anonymous",
                    createdAt = Date.from(Instant.now().minus(100, ChronoUnit.DAYS)),
                    thumbnailURL = null
                ),
                Post(
                    title = "some title",
                    description = "less description",
                    voteCount = 180,
                    commentCount = 22,
                    author = "Anonymous",
                    createdAt = Date.from(Instant.now().minus(366, ChronoUnit.DAYS)),
                    thumbnailURL = null
                )
            )
        }
    }
}