package org.sparcs.soap.buddyTestSupport

import org.sparcs.soap.app.domain.enums.Event
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol

class MockAnalyticsService : AnalyticsServiceProtocol {
    override fun logEvent(event: Event) {}
}