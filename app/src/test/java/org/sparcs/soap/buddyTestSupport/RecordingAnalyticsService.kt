package org.sparcs.soap.buddyTestSupport

import org.sparcs.soap.app.domain.enums.Event
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol

class RecordingAnalyticsService : AnalyticsServiceProtocol {
    val events = mutableListOf<Event>()
    val names: List<String>
        get() = events.map { it.name }

    override fun logEvent(event: Event) {
        events += event
    }
}
