package org.sparcs.soap.App.Domain.Services

import org.sparcs.soap.App.Domain.Enums.Event

interface AnalyticsServiceProtocol {
    fun logEvent(event: Event)
}