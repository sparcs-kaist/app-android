package org.sparcs.soap.app.features.postCompose.event

import org.sparcs.soap.app.domain.enums.Event

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