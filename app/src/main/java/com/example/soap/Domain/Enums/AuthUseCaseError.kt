package com.example.soap.Domain.Enums

sealed class AuthUseCaseError : Throwable() {
    data class SignInFailed(val error: Throwable) : AuthUseCaseError()

    data object SignOutFailed : AuthUseCaseError() {
        private fun readResolve(): Any = SignOutFailed
    }

    data class RefreshFailed(val error: Throwable) : AuthUseCaseError()

    data object NoAccessToken : AuthUseCaseError() {
        private fun readResolve(): Any = NoAccessToken
    }

    override val message: String?
        get() = when (this) {
            is SignInFailed -> "Sign in failed: ${error.localizedMessage}"
            is SignOutFailed -> "Sign out failed."
            is RefreshFailed -> "Token refresh failed: ${error.localizedMessage}"
            is NoAccessToken -> "No access token."
        }
}
