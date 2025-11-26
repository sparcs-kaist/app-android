package com.sparcs.soap.Shared.Mocks

import com.sparcs.soap.Domain.Models.Feed.FeedImage
import com.sparcs.soap.Domain.Models.Feed.FeedPost
import java.util.Date
import java.util.UUID

fun FeedPost.Companion.mock(): FeedPost {
    return FeedPost(
        id = UUID.randomUUID().toString(),
        content = "sample post with image",
        isAnonymous = false,
        isKaistIP = true,
        authorName = "멋진다람쥐632",
        nickname = "멋진다람쥐632",
        profileImageURL = ("https://dlnutnvhcnj0u.cloudfront.net/imgs/NupjukOTL.png"),
        createdAt = Date(),
        commentCount = 0,
        upVotes = 0,
        downVotes = 0,
        myVote = null,
        isAuthor = true,
        images = listOf(
            FeedImage(
                id = UUID.randomUUID().toString(),
                url = ("https://dlnutnvhcnj0u.cloudfront.net/orphaned/07adc127-fb22-42f4-9483-52fdf8e72229.jpg"),
                mimeType = "image/png",
                size = 1258382,
                spoiler = false
            )
        )
    )
}

fun FeedPost.Companion.mockList(): List<FeedPost> {
    return listOf(
        FeedPost(
            id = UUID.randomUUID().toString(),
            content = "sample post with image",
            isAnonymous = false,
            isKaistIP = false,
            authorName = "멋진다람쥐632",
            nickname = "멋진다람쥐632",
            profileImageURL =("https://dlnutnvhcnj0u.cloudfront.net/imgs/NupjukOTL.png"),
            createdAt = Date(),
            commentCount = 0,
            upVotes = 0,
            downVotes = 0,
            myVote = null,
            isAuthor = true,
            images = listOf(
                FeedImage(
                    id = UUID.randomUUID().toString(),
                    url = ("https://dlnutnvhcnj0u.cloudfront.net/orphaned/07adc127-fb22-42f4-9483-52fdf8e72229.jpg"),
                    mimeType = "image/png",
                    size = 1258382,
                    spoiler = false
                )
            )
        ),
        FeedPost(
            id = UUID.randomUUID().toString(),
            content = "sample post",
            isAnonymous = false,
            isKaistIP = false,
            authorName = "멋진다람쥐632",
            nickname = "멋진다람쥐632",
            profileImageURL = ("https://dlnutnvhcnj0u.cloudfront.net/imgs/NupjukOTL.png"),
            createdAt = Date(),
            commentCount = 0,
            upVotes = 0,
            downVotes = 0,
            myVote = null,
            isAuthor = true,
            images = listOf(
                FeedImage(
                    id = UUID.randomUUID().toString(),
                    url = ("https://dlnutnvhcnj0u.cloudfront.net/orphaned/0c351f1b-3837-4329-9ced-de317fcfea6a.jpg"),
                    mimeType = "image/png",
                    size = 219138,
                    spoiler = false
                ),
                FeedImage(
                    id = UUID.randomUUID().toString(),
                    url = ("https://dlnutnvhcnj0u.cloudfront.net/orphaned/07adc127-fb22-42f4-9483-52fdf8e72229.jpg"),
                    mimeType = "image/png",
                    size = 1258382,
                    spoiler = false
                )
            )
        ),
        FeedPost(
            id = UUID.randomUUID().toString(),
            content = "sample post",
            isAnonymous = false,
            isKaistIP = false,
            authorName = "멋진다람쥐632",
            nickname = "멋진다람쥐632",
            profileImageURL = ("https://dlnutnvhcnj0u.cloudfront.net/imgs/NupjukOTL.png"),
            createdAt = Date(),
            commentCount = 0,
            upVotes = 0,
            downVotes = 0,
            myVote = null,
            isAuthor = true,
            images = listOf(
                FeedImage(
                    id = UUID.randomUUID().toString(),
                    url = ("https://dlnutnvhcnj0u.cloudfront.net/orphaned/b899e2e7-d68d-4c94-8c8c-2eae607ec6d2.jpg"),
                    mimeType = "image/png",
                    size = 48507,
                    spoiler = false
                )
            )
        ),
        FeedPost(
            id = UUID.randomUUID().toString(),
            content = "sample post",
            isAnonymous = false,
            isKaistIP = false,
            authorName = "멋진다람쥐632",
            nickname = "멋진다람쥐632",
            profileImageURL = ("https://dlnutnvhcnj0u.cloudfront.net/imgs/NupjukOTL.png"),
            createdAt = Date(),
            commentCount = 0,
            upVotes = 0,
            downVotes = 0,
            myVote = null,
            isAuthor = true,
            images = listOf(
                FeedImage(
                    id = UUID.randomUUID().toString(),
                    url = ("https://dlnutnvhcnj0u.cloudfront.net/orphaned/c54cac50-ff4b-4866-a544-c73b65ca7eb7.jpg"),
                    mimeType = "image/png",
                    size = 15823,
                    spoiler = false
                )
            )
        ),
        FeedPost(
            id = UUID.randomUUID().toString(),
            content = "anonymously shit posting",
            isAnonymous = true,
            isKaistIP = false,
            authorName = "Anonymous",
            nickname = null,
            profileImageURL = null,
            createdAt = Date(),
            commentCount = 0,
            upVotes = 0,
            downVotes = 0,
            myVote = null,
            isAuthor = true,
            images = listOf(
                FeedImage(
                    id = UUID.randomUUID().toString(),
                    url = ("https://dlnutnvhcnj0u.cloudfront.net/orphaned/0c351f1b-3837-4329-9ced-de317fcfea6a.jpg"),
                    mimeType = "image/png",
                    size = 219138,
                    spoiler = false
                )
            )
        )
    )
}