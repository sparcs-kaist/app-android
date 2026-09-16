package org.sparcs.soap.app.domain.usecases

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.sparcs.soap.app.domain.error.CrashContext
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.helpers.TimetableThemeShareCode
import org.sparcs.soap.app.domain.repositories.settings.TimetableThemeRepository
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
import javax.inject.Inject

class TimetableThemeUseCase @Inject constructor(
    private val repository: TimetableThemeRepository,
    private val crashlytics: CrashlyticsServiceProtocol,
) {
    suspend fun share(theme: TimetableTheme): String = execute("share") {
        require(theme.isValid)
        requireNotNull(TimetableThemeShareCode.normalized(repository.share(theme)))
    }

    suspend fun fetch(input: String): TimetableTheme {
        val code = requireNotNull(TimetableThemeShareCode.normalized(input))
        return execute("fetch") { repository.fetch(code) }
    }

    private suspend fun <T> execute(operation: String, action: suspend () -> T): T = try {
        action()
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        currentCoroutineContext().ensureActive()
        crashlytics.record(error, CrashContext("TimetableTheme", action = operation))
        throw error
    }
}
