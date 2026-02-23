package org.sparcs.soap.App.Features.FeedPost.Event

import org.sparcs.soap.App.Domain.Enums.Event

sealed class FeedPostViewEvent : Event {
    data class CommentSubmitted(val isReply: Boolean, val isAnonymous: Boolean) : FeedPostViewEvent()
    data class PostReported(val reason: String) : FeedPostViewEvent()
    object PostDeleteConfirmed : FeedPostViewEvent()
    object CommentsRefreshed : FeedPostViewEvent()

    override val source: String
        get() = "FeedPostView"

    override val name: String
        get() = when (this) {
            is CommentSubmitted -> "comment_submitted"
            is PostReported -> "post_reported"
            is PostDeleteConfirmed -> "post_delete_confirmed"
            is CommentsRefreshed -> "comments_refreshed"
        }

    override val parameters: Map<String, Any>
        get() = when (this) {
            is CommentSubmitted -> mapOf(
                "source" to source,
                "is_reply" to isReply,
                "is_anonymous" to isAnonymous
            )
            is PostReported -> mapOf(
                "source" to source,
                "reason" to reason
            )
            is PostDeleteConfirmed -> mapOf("source" to source)
            is CommentsRefreshed -> mapOf("source" to source)
        }
}