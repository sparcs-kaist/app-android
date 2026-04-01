package org.sparcs.soap.App.Domain.Error

import android.content.Context
import org.sparcs.soap.R
import java.io.Serializable

sealed class NetworkError : Exception(), SourcedError, Serializable {
    override val source: ErrorSource = ErrorSource.Network

    object NoConnection : NetworkError() {
        private fun readResolve(): Any = NoConnection
    }

    object Timeout : NetworkError() {
        private fun readResolve(): Any = Timeout
    }

    data class ServerError(val code: Int, override val message: String? = null) : NetworkError()

    object Unauthorized : NetworkError() {
        private fun readResolve(): Any = Unauthorized
    }

    object NotFound : NetworkError() {
        private fun readResolve(): Any = NotFound
    }

    data class Unknown(val underlying: Throwable) : NetworkError()

    val messageArgs: Array<Any>
        get() = when (this) {
            is ServerError -> arrayOf(code)
            else -> emptyArray()
        }

    val messageRes: Int
        get() = when (this) {
            is NoConnection -> R.string.error_no_connection
            is Timeout -> R.string.error_timeout
            is ServerError -> R.string.error_server_error
            is Unauthorized -> R.string.error_unauthorized
            is NotFound -> R.string.error_not_found
            is Unknown -> R.string.error_unknown
        }

    val isRetryable: Boolean
        get() = when (this) {
            is Timeout -> true
            is ServerError -> code >= 500
            else -> false
        }

    val isRecordable: Boolean
        get() = when (this) {
            is ServerError, is NotFound, is Unknown -> true
            else -> false
        }

    fun message(context: Context): String =
        context.getString(messageRes, *messageArgs)
}