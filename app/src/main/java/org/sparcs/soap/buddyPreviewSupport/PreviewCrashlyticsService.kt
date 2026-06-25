package org.sparcs.soap.buddyPreviewSupport

import org.sparcs.soap.app.domain.error.CrashContext
import org.sparcs.soap.app.domain.error.SourcedError
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol

class PreviewCrashlyticsService : CrashlyticsServiceProtocol {
    override fun recordException(error: Throwable) {}
    override fun record(error: SourcedError, context: CrashContext) {}
    override fun record(error: Throwable, context: CrashContext) { }
}