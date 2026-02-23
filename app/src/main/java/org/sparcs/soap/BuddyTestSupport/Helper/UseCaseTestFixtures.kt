package org.sparcs.soap.BuddyTestSupport.Helper

import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Models.Feed.FeedComment
import org.sparcs.soap.App.Domain.Models.Feed.FeedCreateComment
import org.sparcs.soap.App.Domain.Models.Feed.FeedCreatePost
import org.sparcs.soap.App.Domain.Models.Feed.FeedImage
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Domain.Models.Feed.FeedPostPage
import java.util.Date

object UseCaseTestFixtures {
    fun makePost(
        id: String = "post-1",
        content: String = "Test content",
        upVotes: Int = 5,
        downVotes: Int = 2,
        myVote: FeedVoteType? = null
    ): FeedPost {
        return FeedPost(
            id = id,
            content = content,
            isAnonymous = false,
            isKaistIP = true,
            authorName = "Test Author",
            nickname = "tester",
            profileImageURL = null,
            createdAt = Date(),
            commentCount = 0,
            upVotes = upVotes,
            downVotes = downVotes,
            myVote = myVote,
            isAuthor = false,
            images = emptyList()
        )
    }

    fun makeComment(
        id: String = "comment-1",
        postID: String = "post-1",
        parentCommentID: String? = null,
        content: String = "Test comment"
    ): FeedComment {
        return FeedComment(
            id = id,
            postID = postID,
            parentCommentID = parentCommentID,
            content = content,
            isDeleted = false,
            isAnonymous = false,
            isKaistIP = true,
            authorName = "Test Commenter",
            isAuthor = false,
            isMyComment = false,
            profileImageURL = null,
            createdAt = Date(),
            upVotes = 3,
            downVotes = 1,
            myVote = null,
            image = null,
            replyCount = 0,
            replies = emptyList()
        )
    }

    fun makePostPage(
        posts: List<FeedPost> = emptyList(),
        nextCursor: String? = null,
        hasNext: Boolean = false
    ): FeedPostPage {
        return FeedPostPage(
            items = posts,
            nextCursor = nextCursor,
            hasNext = hasNext
        )
    }

    fun makeCreatePost(
        content: String = "New post content",
        isAnonymous: Boolean = false,
        images: List<FeedImage> = emptyList()
    ): FeedCreatePost {
        return FeedCreatePost(
            content = content,
            isAnonymous = isAnonymous,
            images = images
        )
    }

    fun makeCreateComment(
        content: String = "New comment",
        isAnonymous: Boolean = false,
        image: FeedImage? = null
    ): FeedCreateComment {
        return FeedCreateComment(
            content = content,
            isAnonymous = isAnonymous,
            image = image
        )
    }
}