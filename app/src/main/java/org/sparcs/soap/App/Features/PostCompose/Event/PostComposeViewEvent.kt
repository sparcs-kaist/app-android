package org.sparcs.soap.App.Features.PostCompose.Event

import org.sparcs.soap.App.Domain.Enums.Event

sealed class PostComposeViewEvent : Event {
    data object PostSubmitted : PostComposeViewEvent()

    override val source: String = "PostComposeView"

    override val name: String
        get() = when (this) {
            is PostSubmitted -> "post_submitted"
        }

    override val parameters: Map<String, Any>
        get() = mapOf("source" to source)
}