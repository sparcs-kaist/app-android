package com.sparcs.soap.Domain.Enums

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

    override val message: String?
        get() = when (this) {
            is UserCancelled -> "Authentication was cancelled by the user."
            is InvalidCallbackURL -> "Invalid callback URL received."
            is TokenExchangeFailed -> "Failed to exchange code for tokens: ${error.localizedMessage}"
            is TokenRefreshFailed -> "Failed to refresh access token: ${error.localizedMessage}"
            is NoRefreshTokenAvailable -> "No refresh token available."
            is Unknown -> "An unknown authentication error occurred."
        }
}