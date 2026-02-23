package org.sparcs.soap.BuddyTestSupport

import org.sparcs.soap.App.Domain.Enums.Event
import org.sparcs.soap.App.Domain.Services.AnalyticsServiceProtocol

class MockAnalyticsService : AnalyticsServiceProtocol {
    override fun logEvent(event: Event) {}
}