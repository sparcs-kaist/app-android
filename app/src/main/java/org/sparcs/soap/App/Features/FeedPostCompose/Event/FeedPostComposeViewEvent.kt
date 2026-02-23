package org.sparcs.soap.App.Features.FeedPostCompose.Event

import org.sparcs.soap.App.Domain.Enums.Event

sealed class FeedPostComposeViewEvent : Event {
    data class PostSubmitted(val isAnonymous: Boolean, val imageCount: Int) : FeedPostComposeViewEvent()
    data class ComposeTypeChanged(val type: String) : FeedPostComposeViewEvent()

    override val source: String
        get() = "FeedPostComposeView"

    override val name: String
        get() = when (this) {
            is PostSubmitted -> "post_submitted"
            is ComposeTypeChanged -> "compose_type_changed"
        }

    override val parameters: Map<String, Any>
        get() = when (this) {
            is PostSubmitted -> mapOf(
                "source" to source,
                "is_anonymous" to isAnonymous,
                "image_count" to imageCount
            )
            is ComposeTypeChanged -> mapOf(
                "source" to source,
                "compose_type" to type
            )
        }
}