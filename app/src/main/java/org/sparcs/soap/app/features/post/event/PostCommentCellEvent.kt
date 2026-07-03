package org.sparcs.soap.app.features.post.event

import org.sparcs.soap.app.domain.enums.Event

sealed class PostCommentCellEvent : Event {
    data object CommentUpVoted : PostCommentCellEvent()
    data object CommentDownVoted : PostCommentCellEvent()
    data class CommentReported(val type: String) : PostCommentCellEvent()
    data object CommentDeleted : PostCommentCellEvent()

    override val source: String = "PostCommentCell"

    override val name: String
        get() = when (this) {
            is CommentUpVoted -> "comment_upvoted"
            is CommentDownVoted -> "comment_downvoted"
            is CommentReported -> "comment_reported"
            is CommentDeleted -> "comment_deleted"
        }

    override val parameters: Map<String, Any>
        get() = when (this) {
            is CommentReported -> mapOf(
                "source" to source,
                "type" to type
            )
            else -> mapOf("source" to source)
        }
}