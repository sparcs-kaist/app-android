package com.sparcs.soap.Shared.Mocks

import com.sparcs.soap.Domain.Enums.Feed.FeedVoteType
import com.sparcs.soap.Domain.Models.Feed.FeedComment
import com.sparcs.soap.Domain.Models.Feed.FeedImage
import java.util.Date
import java.util.UUID

fun FeedComment.Companion.mock(): FeedComment {
    return FeedComment(
        id = UUID.randomUUID().toString(),
        postID = UUID.randomUUID().toString(),
        parentCommentID = null,
        content = "샘플 댓글입니다. 이 댓글은 테스트 목적으로 사용됩니다.",
        isDeleted = false,
        isKaistIP = true,
        isAnonymous = false,
        authorName = "멋진호랑이677",
        isAuthor = false,
        isMyComment = true,
        profileImageURL = ("https://dlnutnvhcnj0u.cloudfront.net/imgs/CatOTL.png"),
        createdAt = Date(),
        upVotes = 5,
        downVotes = 1,
        myVote = FeedVoteType.UP,
        image = null,
        replyCount = 2,
        replies = emptyList()
    )
}

fun FeedComment.Companion.mockList(): List<FeedComment> {
    return listOf(
        FeedComment(
            id = "2084eb46-cae5-4a87-8b08-4246680c1dbc",
            postID = "4680556d-5db8-46ed-bc52-8fb859885bd6",
            parentCommentID = null,
            content = "string",
            isDeleted = false,
            isKaistIP = false,
            isAnonymous = false,
            authorName = "멋진호랑이677",
            isAuthor = true,
            isMyComment = true,
            profileImageURL = ("https://dlnutnvhcnj0u.cloudfront.net/imgs/CatOTL.png"),
            createdAt = Date(),
            upVotes = 0,
            downVotes = 0,
            myVote = null,
            image = null,
            replyCount = 1,
            replies = listOf(
                FeedComment(
                    id = "812a6383-636c-4d85-9704-b2a49b0a43fd",
                    postID = "4680556d-5db8-46ed-bc52-8fb859885bd6",
                    parentCommentID = "2084eb46-cae5-4a87-8b08-4246680c1dbc",
                    content = "(deleted)",
                    isDeleted = true,
                    isKaistIP = false,
                    isAnonymous = false,
                    authorName = "멋진호랑이677",
                    isAuthor = false,
                    isMyComment = true,
                    profileImageURL = ("https://dlnutnvhcnj0u.cloudfront.net/imgs/CatOTL.png"),
                    createdAt = Date(),
                    upVotes = 0,
                    downVotes = 0,
                    myVote = null,
                    image = null,
                    replyCount = 0,
                    replies = emptyList()
                )
            )
        ),
        FeedComment(
            id = "395749fe-6e1f-48cb-a144-a906992e34a6",
            postID = "4680556d-5db8-46ed-bc52-8fb859885bd6",
            parentCommentID = null,
            content = "test comments",
            isDeleted = false,
            isKaistIP = false,
            isAnonymous = true,
            authorName = "Anonymous 1",
            isAuthor = false,
            isMyComment = true,
            profileImageURL = ("https://dlnutnvhcnj0u.cloudfront.net/imgs/CatOTL.png"),
            createdAt = Date(),
            upVotes = 0,
            downVotes = 0,
            myVote = null,
            image = null,
            replyCount = 0,
            replies = emptyList()
        ),
        FeedComment(
            id = "bb0a2224-b57a-42b6-8d81-f9b4af382ea1",
            postID = "4680556d-5db8-46ed-bc52-8fb859885bd6",
            parentCommentID = null,
            content = "test comments with an image",
            isDeleted = false,
            isKaistIP = false,
            isAnonymous = true,
            authorName = "Anonymous 1",
            isAuthor = false,
            isMyComment = true,
            profileImageURL = ("https://dlnutnvhcnj0u.cloudfront.net/imgs/CatOTL.png"),
            createdAt = Date(),
            upVotes = 0,
            downVotes = 0,
            myVote = null,
            image = FeedImage(
                id = "1dc53a00-baf4-4dcb-8b7a-454e5fc9aad0",
                url = ("https://dlnutnvhcnj0u.cloudfront.net/orphaned/9015ab49-8261-477a-960c-715beac38af1.jpg"),
                mimeType = "image/png",
                size = 1258382,
                spoiler = false
            ),
            replyCount = 0,
            replies = emptyList()
        )
    )
}