package com.sparcs.soap.Shared.Mocks

import com.sparcs.soap.Domain.Models.Ara.Post
import java.net.URL
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

fun Post.Companion.mockList(): List<Post>{
    return listOf(
        Post(
            title = "some title",
            description = "verrrry loooonnngggg description",
            voteCount = 12,
            commentCount = 15,
            author = "ayayayayaya",
            createdAt = Date(),
            thumbnailURL = URL("https://d2phebdq64jyfk.cloudfront.net/media/article/8efac7c4150a4d67affcd29e509e9e78.png")
        ),
        Post(
            title = "제목",
            description = "설명설명설명",
            voteCount = -2,
            commentCount = 30,
            author = "거위",
            createdAt = Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)),
            thumbnailURL = null
        ),
        Post(
            title = "some title",
            description = "verrrry loooonnngggg asdfojapsdofpoweufpoqiewfpoqiuwepfoiquwepfoiquwepfoiqwuepfoiqwuepfoiquwepfoiquwepofiquwepofiuqpwoeifuqpwoeifuqpwoeifu",
            voteCount = 12,
            commentCount = 1,
            author = "goose",
            createdAt = Date.from(Instant.now().minus(2, ChronoUnit.MINUTES)),
            thumbnailURL = null
        ),
        Post(
            title = "some title",
            description = "verrrry loooonnngggg asdfojapsdofpoweufpoqiewfpoqiuwepfoiquwepfoiquwepfoiqwuepfoiqwuepfoiquwepfoiquwepofiquwepofiuqpwoeifuqpwoeifuqpwoeifu",
            voteCount = 0,
            commentCount = 0,
            author = "goose",
            createdAt = Date.from(Instant.now().minus(1, ChronoUnit.HOURS)),
            thumbnailURL = null
        ),
        Post(
            title = "some title",
            description = "verrrry loooonnngggg asdfojapsdofpoweufpoqiewfpoqiuwepfoiquwepfoiquwepfoiqwuepfoiqwuepfoiquwepfoiquwepofiquwepofiuqpwoeifuqpwoeifuqpwoeifu",
            voteCount = -2,
            commentCount = 30,
            author = "goose",
            createdAt = Date.from(Instant.now().minus(12, ChronoUnit.HOURS)),
            thumbnailURL = null
        ),
        Post(
            title = "some title",
            description = "verrrry loooonnngggg asdfojapsdofpoweufpoqiewfpoqiuwepfoiquwepfoiquwepfoiqwuepfoiqwuepfoiquwepfoiquwepofiquwepofiuqpwoeifuqpwoeifuqpwoeifu",
            voteCount = -20,
            commentCount = 30,
            author = "goose",
            createdAt = Date.from(Instant.now().minus(3, ChronoUnit.DAYS)),
            thumbnailURL = null
        ),
        Post(
            title = "mooooooooooooore tiiiiiiiiiitlellllllllllllllllllllllllllllllllllsdainocyrnadCHASDADJAHNDVLACD",
            description = "verrrry loooonnngggg description",
            voteCount = 180,
            commentCount = 8,
            author = "nupzuki",
            createdAt = Date.from(Instant.now().minus(7, ChronoUnit.DAYS)),
            thumbnailURL = null
        ),
        Post(
            title = "some title",
            description = "less description",
            voteCount = 180,
            commentCount = 0,
            author = "Anonymous",
            createdAt = Date.from(Instant.now().minus(100, ChronoUnit.DAYS)),
            thumbnailURL = null
        ),
        Post(
            title = "some title",
            description = "less description",
            voteCount = 0,
            commentCount = 2,
            author = "Anonymous",
            createdAt = Date.from(Instant.now().minus(366, ChronoUnit.DAYS)),
            thumbnailURL = null
        ),
        Post(
            title = "some title title",
            description = "less description",
            voteCount = 0,
            commentCount = 0,
            author = "Anonymous",
            createdAt = Date.from(Instant.now().minus(1000, ChronoUnit.DAYS)),
            thumbnailURL = null
        )
    )
}
