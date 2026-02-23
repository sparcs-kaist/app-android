package org.sparcs.soap.App.Domain.Services

import org.sparcs.soap.App.Domain.Error.CrashContext
import org.sparcs.soap.App.Domain.Error.SourcedError

interface CrashlyticsServiceProtocol {
    fun recordException(error: Throwable)

    fun record(error: SourcedError, context: CrashContext)
    fun record(error: Throwable, context: CrashContext)
}