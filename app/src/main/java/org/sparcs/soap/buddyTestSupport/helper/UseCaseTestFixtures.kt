package org.sparcs.soap.buddyTestSupport.helper

import org.sparcs.soap.app.domain.enums.feed.FeedVoteType
import org.sparcs.soap.app.domain.models.feed.FeedComment
import org.sparcs.soap.app.domain.models.feed.FeedCreateComment
import org.sparcs.soap.app.domain.models.feed.FeedCreatePost
import org.sparcs.soap.app.domain.models.feed.FeedImage
import org.sparcs.soap.app.domain.models.feed.FeedPost
import org.sparcs.soap.app.domain.models.feed.FeedPostPage
import java.util.Date

object UseCaseTestFixtures {
    fun makePost(
        id: String = "post-1",
        content: String = "Test content",
        upVotes: Int = 5,
        downVotes: Int = 2,
        myVote: FeedVoteType? = null,
        commentCount: Int = 0,
        isAuthor: Boolean = false
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
            commentCount = commentCount,
            upVotes = upVotes,
            downVotes = downVotes,
            myVote = myVote,
            isAuthor = isAuthor,
            images = emptyList()
        )
    }

    fun makeComment(
        id: String = "comment-1",
        postID: String = "post-1",
        parentCommentID: String? = null,
        content: String = "Test comment",
        upVotes: Int = 3,
        downVotes: Int = 1,
        myVote: FeedVoteType? = null,
        isDeleted: Boolean = false,
        isAuthor: Boolean = false
    ): FeedComment {
        return FeedComment(
            id = id,
            postID = postID,
            parentCommentID = parentCommentID,
            content = content,
            isDeleted = isDeleted,
            isAnonymous = false,
            isKaistIP = true,
            authorName = "Test Commenter",
            isAuthor = isAuthor,
            isMyComment = isAuthor,
            profileImageURL = null,
            createdAt = Date(),
            upVotes = upVotes,
            downVotes = downVotes,
            myVote = myVote,
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