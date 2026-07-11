package org.sparcs.soap.buddyTestSupport

import org.sparcs.soap.app.domain.error.CrashContext
import org.sparcs.soap.app.domain.error.SourcedError
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol

class MockCrashlyticsService : CrashlyticsServiceProtocol {
    var recordExceptionCallCount = 0
    var recordErrorWithContextCallCount = 0
    var lastRecordedError: Throwable? = null
    var lastRecordedContext: CrashContext? = null

    override fun recordException(error: Throwable) {
        recordExceptionCallCount += 1
        lastRecordedError = error
    }

    override fun record(error: SourcedError, context: CrashContext) {
        recordErrorWithContextCallCount += 1
        lastRecordedError = error as Throwable
        lastRecordedContext = context
    }

    override fun record(error: Throwable, context: CrashContext) {
        recordErrorWithContextCallCount += 1
        lastRecordedError = error
        lastRecordedContext = context
    }
}