package org.sparcs.soap.App.Features.Feed.Event

import org.sparcs.soap.App.Domain.Enums.Event

sealed class FeedPostRowEvent : Event {
    data object PostUpVoted : FeedPostRowEvent()
    data object PostDownVoted : FeedPostRowEvent()
    data class PostReported(val reason: String) : FeedPostRowEvent()

    override val source: String
        get() = "FeedPostRow"

    override val name: String
        get() = when (this) {
            is PostUpVoted -> "post_upVoted"
            is PostDownVoted -> "post_downVoted"
            is PostReported -> "post_reported"
        }

    override val parameters: Map<String, Any>
        get() = when (this) {
            is PostUpVoted -> mapOf("source" to source)
            is PostDownVoted -> mapOf("source" to source)
            is PostReported -> mapOf(
                "source" to source,
                "reason" to reason
            )
        }
}