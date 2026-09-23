package org.sparcs.soap.app.domain.helpers

import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.error.auth.AuthUseCaseError
import org.sparcs.soap.app.domain.error.auth.AuthenticationServiceError
import org.sparcs.soap.app.domain.error.otl.TimetableUseCaseError
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException

internal fun Throwable.timetableCause(): Throwable {
    var current = this
    val seen = mutableSetOf<Throwable>()
    while (seen.add(current)) {
        current = when (current) {
            is AuthUseCaseError.RefreshFailed -> current.error
            is AuthenticationServiceError.TokenRefreshFailed -> current.error
            is TimetableUseCaseError.Unknown -> current.underlying
            is NetworkError.Unknown -> current.underlying
            else -> current.cause
        } ?: return current
    }
    return current
}

internal fun Throwable.isOfflineFailure(): Boolean = when (timetableCause()) {
    is NetworkError.NoConnection, is UnknownHostException, is ConnectException -> true
    else -> false
}

internal fun Throwable.canKeepSavedTimetable(): Boolean = when (val error = timetableCause()) {
    is NetworkError.Unauthorized, is NetworkError.NotFound, is AuthUseCaseError.NoAccessToken -> false
    is HttpException -> error.code() !in listOf(400, 401, 403, 404)
    is NetworkError.ServerError -> error.code >= 500
    else -> true
}
