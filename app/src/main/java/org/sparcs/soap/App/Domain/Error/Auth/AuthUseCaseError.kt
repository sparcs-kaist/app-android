package org.sparcs.soap.App.Domain.Error.Auth

import android.content.Context
import org.sparcs.soap.R

sealed class AuthUseCaseError : Exception() {
    data class SignInFailed(val error: Throwable) : AuthUseCaseError()

    data object SignOutFailed : AuthUseCaseError() {
        private fun readResolve(): Any = SignOutFailed
    }

    data class RefreshFailed(val error: Throwable) : AuthUseCaseError()

    data object NoAccessToken : AuthUseCaseError() {
        private fun readResolve(): Any = NoAccessToken
    }

    fun message(context: Context): String {
        return when (this) {
            is SignInFailed -> {
                error.asServiceError()?.message(context)
                    ?: context.getString(R.string.error_sign_in_failed, error.localizedMessage)
            }

            is SignOutFailed -> context.getString(R.string.error_sign_out_failed)
            is RefreshFailed -> {
                error.asServiceError()?.message(context)
                    ?: context.getString(
                        R.string.error_token_refresh_failed,
                        error.localizedMessage
                    )
            }

            is NoAccessToken -> context.getString(R.string.error_no_access_token)
        }
    }
}

private fun Throwable.asServiceError() = this as? AuthenticationServiceError