package org.sparcs.soap.App.Shared.Extensions

import org.sparcs.soap.App.Domain.Error.NetworkError
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.R
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Exception.isNetworkError(): Boolean {
    if (this is NetworkError) {
        return when (this) {
            is NetworkError.NoConnection,
            is NetworkError.Timeout -> true
            else -> false
        }
    }

    val isStandardNetworkError = when (this) {
        is UnknownHostException,
        is SocketTimeoutException,
        is ConnectException -> true
        is IOException -> {
            val msg = message?.lowercase() ?: ""
            listOf("network", "host", "connection", "timeout").any { msg.contains(it) }
        }
        else -> false
    }
    return isStandardNetworkError || (cause as? Exception)?.isNetworkError() ?: false
}

fun Exception.toAlertState(defaultMessageRes: Int): AlertState {
    return when {
        this.isNetworkError() -> AlertState(
            messageResId = R.string.network_connection_error,
            message = this.localizedMessage
        )

        else -> AlertState(
            messageResId = defaultMessageRes,
            message = this.localizedMessage
        )
    }
}