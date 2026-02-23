package org.sparcs.soap.App.Features.Post.Event

import org.sparcs.soap.App.Domain.Enums.Event

sealed class PostViewEvent : Event {
    data object PostUpVoted : PostViewEvent()
    data object PostDownVoted : PostViewEvent()
    data class BookmarkToggled(val isBookmarked: Boolean) : PostViewEvent()
    data object CommentSubmitted : PostViewEvent()
    data class PostReported(val type: String) : PostViewEvent()
    data object PostDeleted : PostViewEvent()
//    data object SummariseRequested : PostViewEvent()

    override val source: String = "PostView"

    override val name: String
        get() = when (this) {
            is PostUpVoted -> "post_upvoted"
            is PostDownVoted -> "post_downvoted"
            is BookmarkToggled -> "bookmark_toggled"
            is CommentSubmitted -> "comment_submitted"
            is PostReported -> "post_reported"
            is PostDeleted -> "post_deleted"
//            is SummariseRequested -> "summarise_requested"
        }

    override val parameters: Map<String, Any>
        get() = when (this) {
            is BookmarkToggled -> mapOf(
                "source" to source,
                "isBookmarked" to isBookmarked.toString()
            )
            is PostReported -> mapOf(
                "source" to source,
                "type" to type
            )
            else -> mapOf("source" to source)
        }
}