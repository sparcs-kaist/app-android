package org.sparcs.soap.app.features.feed.event

import org.sparcs.soap.app.domain.enums.Event

sealed class FeedViewEvent : Event {
    data object FeedRefreshed : FeedViewEvent()
    data object WriteFeedButtonTapped : FeedViewEvent()
    data object OpenSettingsButtonTapped : FeedViewEvent()

    override val source: String
        get() = "FeedView"

    override val name: String
        get() = when (this) {
            is FeedRefreshed -> "feed_refreshed"
            is WriteFeedButtonTapped -> "write_feed_button_tapped"
            is OpenSettingsButtonTapped -> "open_settings_button_tapped"
        }

    override val parameters: Map<String, Any>
        get() = mapOf("source" to source)
}