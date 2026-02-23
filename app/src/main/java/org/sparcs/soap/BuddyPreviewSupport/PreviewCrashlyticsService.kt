package org.sparcs.soap.BuddyPreviewSupport

import org.sparcs.soap.App.Domain.Error.CrashContext
import org.sparcs.soap.App.Domain.Error.SourcedError
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol

class PreviewCrashlyticsService : CrashlyticsServiceProtocol {
    override fun recordException(error: Throwable) {}
    override fun record(error: SourcedError, context: CrashContext) {}
    override fun record(error: Throwable, context: CrashContext) { }
}