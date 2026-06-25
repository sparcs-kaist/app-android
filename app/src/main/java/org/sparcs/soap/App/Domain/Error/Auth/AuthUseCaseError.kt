package org.sparcs.soap.App.Domain.Error.Auth

import org.sparcs.soap.R

sealed class AuthUseCaseError : Exception() {
    data class SignInFailed(val error: Throwable) : AuthUseCaseError()

    class SignOutFailed : AuthUseCaseError()

    data class RefreshFailed(val error: Throwable) : AuthUseCaseError()

    class NoAccessToken : AuthUseCaseError()

    val messageRes: Int
        get() = when (this) {
            is SignInFailed -> (error as? AuthenticationServiceError)?.messageRes ?: R.string.error_sign_in_failed
            is RefreshFailed -> (error as? AuthenticationServiceError)?.messageRes ?: R.string.error_token_refresh_failed
            is SignOutFailed -> R.string.error_sign_out_failed
            is NoAccessToken -> R.string.error_no_access_token
        }

    override val message: String?
        get() = when (this) {
            is SignInFailed -> error.localizedMessage
            is RefreshFailed -> error.localizedMessage
            else -> super.message
    }
}

private fun Throwable.asServiceError() = this as? AuthenticationServiceError