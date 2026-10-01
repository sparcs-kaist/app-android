package org.sparcs.soap.app.domain.repositories

import org.sparcs.soap.app.domain.error.auth.AuthenticationServiceError
import org.sparcs.soap.app.networking.responseDTO.auth.SignInResponseDTO
import org.sparcs.soap.app.networking.responseDTO.auth.TokenResponseDTO
import org.sparcs.soap.app.networking.retrofitAPI.AuthApi
import org.sparcs.soap.app.networking.retrofitAPI.LogoutRequest
import org.sparcs.soap.app.networking.retrofitAPI.TokenIssueRequest
import org.sparcs.soap.app.networking.retrofitAPI.TokenRefreshRequest
import javax.inject.Inject
import javax.inject.Named

interface AuthRepositoryProtocol {
    suspend fun requestToken(session: String, codeVerifier: String): SignInResponseDTO
    suspend fun refreshToken(refreshToken: String): TokenResponseDTO
    suspend fun logout(refreshToken: String)
}

class AuthRepository @Inject constructor(
    @param:Named("Auth") private val authApi: AuthApi
) : AuthRepositoryProtocol {

    override suspend fun requestToken(session: String, codeVerifier: String): SignInResponseDTO {
        try {
            return authApi.requestTokens(
                body = TokenIssueRequest(
                    session = session,
                    codeVerifier = codeVerifier
                )
            )
        } catch (_: Exception) {
            throw AuthenticationServiceError.Unknown()
        }
    }

    override suspend fun refreshToken(refreshToken: String): TokenResponseDTO {
        return authApi.refreshTokens(body = TokenRefreshRequest(refreshToken = refreshToken))
    }

    override suspend fun logout(refreshToken: String) {
        try {
            authApi.logout(body = LogoutRequest(refreshToken = refreshToken))
        } catch (_: Exception) {
            // Ignore logout API failures
        }
    }
}
