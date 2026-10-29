package org.sparcs.soap.app.features.post.event

import org.sparcs.soap.app.domain.enums.Event

sealed class PostViewEvent : Event {
    data object PostUpVoted : PostViewEvent()
    data object PostDownVoted : PostViewEvent()
    data class BookmarkToggled(val isBookmarked: Boolean) : PostViewEvent()
    data object CommentSubmitted : PostViewEvent()
    data class PostReported(val type: String) : PostViewEvent()
    data object PostDeleted : PostViewEvent()
    data object SummaryRequested : PostViewEvent()
    data object SummaryFinished : PostViewEvent()
    data class SummaryFailed(val reason: String) : PostViewEvent()
    data class TranslationRequested(val target: String) : PostViewEvent()
    data class AuthorBlockToggled(val isBlocked: Boolean) : PostViewEvent()

    override val source: String = "PostView"

    override val name: String
        get() = when (this) {
            is PostUpVoted -> "post_upvoted"
            is PostDownVoted -> "post_downvoted"
            is BookmarkToggled -> "bookmark_toggled"
            is CommentSubmitted -> "comment_submitted"
            is PostReported -> "post_reported"
            is PostDeleted -> "post_deleted"
            is SummaryRequested -> "summarise_requested"
            is SummaryFinished -> "post_summary_finished"
            is SummaryFailed -> "post_summary_failed"
            is TranslationRequested -> "translation_requested"
            is AuthorBlockToggled -> "author_block_toggled"
        }

    override val parameters: Map<String, Any>
        get() = when (this) {
            is BookmarkToggled -> mapOf("source" to source, "isBookmarked" to isBookmarked.toString())
            is AuthorBlockToggled -> mapOf("source" to source, "isBlocked" to isBlocked.toString())
            is PostReported -> mapOf("source" to source, "type" to type)
            is TranslationRequested -> mapOf("source" to source, "target" to target)
            is SummaryFailed -> mapOf("source" to source, "reason" to reason)
            else -> mapOf("source" to source)
        }
}
