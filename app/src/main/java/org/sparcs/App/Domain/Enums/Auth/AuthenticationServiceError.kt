package org.sparcs.App.Domain.Enums.Auth

import android.content.Context
import org.sparcs.R

sealed class AuthenticationServiceError : Exception() {
    data object UserCancelled : AuthenticationServiceError() {
        private fun readResolve(): Any = UserCancelled
    }

    data object InvalidCallbackURL : AuthenticationServiceError() {
        private fun readResolve(): Any = InvalidCallbackURL
    }

    data class TokenExchangeFailed(val error: Throwable) : AuthenticationServiceError()

    data class TokenRefreshFailed(val error: Throwable) : AuthenticationServiceError()

    data object NoRefreshTokenAvailable : AuthenticationServiceError() {
        private fun readResolve(): Any = NoRefreshTokenAvailable
    }

    data object Unknown : AuthenticationServiceError() {
        private fun readResolve(): Any = Unknown
    }

    fun message(context: Context): String {
        return when (this) {
            is UserCancelled -> context.getString(R.string.error_user_cancelled)
            is InvalidCallbackURL -> context.getString(R.string.error_invalid_callback_url)
            is TokenExchangeFailed -> context.getString(
                R.string.error_token_exchange_failed,
                error.localizedMessage
            )

            is TokenRefreshFailed -> context.getString(
                R.string.error_token_refresh_failed,
                error.localizedMessage
            )

            is NoRefreshTokenAvailable -> context.getString(R.string.error_no_refresh_token)
            is Unknown -> context.getString(R.string.error_unknown)
        }
    }
}