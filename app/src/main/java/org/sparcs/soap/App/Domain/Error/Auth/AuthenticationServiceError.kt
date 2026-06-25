package org.sparcs.soap.App.Domain.Error.Auth

import org.sparcs.soap.R

sealed class AuthenticationServiceError : Exception() {
    class UserCancelled : AuthenticationServiceError()

    class InvalidCallbackURL : AuthenticationServiceError()

    data class TokenExchangeFailed(val error: Throwable) : AuthenticationServiceError()

    data class TokenRefreshFailed(val error: Throwable) : AuthenticationServiceError()

    class NoRefreshTokenAvailable : AuthenticationServiceError()

    class Unknown : AuthenticationServiceError()

    val messageRes: Int
        get() = when (this) {
            is UserCancelled -> R.string.error_user_cancelled
            is InvalidCallbackURL -> R.string.error_invalid_callback_url
            is TokenExchangeFailed -> R.string.error_token_exchange_failed
            is TokenRefreshFailed -> R.string.error_token_refresh_failed
            is NoRefreshTokenAvailable -> R.string.error_no_refresh_token
            else -> R.string.error_unknown
        }

    override val message: String?
        get() = when (this) {
            is TokenExchangeFailed -> error.localizedMessage
            is TokenRefreshFailed -> error.localizedMessage
            else -> super.message
        }
}