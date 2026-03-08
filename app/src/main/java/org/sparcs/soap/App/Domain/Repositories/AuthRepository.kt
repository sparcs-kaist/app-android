package org.sparcs.soap.App.Domain.Repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sparcs.soap.App.Domain.Error.Auth.AuthenticationServiceError
import org.sparcs.soap.App.Networking.ResponseDTO.Auth.SignInResponseDTO
import org.sparcs.soap.App.Networking.ResponseDTO.Auth.TokenResponseDTO
import org.sparcs.soap.App.Networking.RetrofitAPI.AuthApi
import org.sparcs.soap.App.Shared.Extensions.base64UrlEncodedString
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Named

interface AuthRepositoryProtocol {
    suspend fun requestToken(authorisationCode: String, codeVerifier: String): SignInResponseDTO
    suspend fun refreshToken(refreshToken: String): TokenResponseDTO
}

class AuthRepository @Inject constructor(
    @Named("Auth") private val authApi: AuthApi
) : AuthRepositoryProtocol {

    override suspend fun requestToken(authorisationCode: String, codeVerifier: String): SignInResponseDTO {
        try {
            val encodedSessionCode = withContext(Dispatchers.IO) {
                URLEncoder.encode(authorisationCode, "UTF-8")
            }
            val encodedVerifier =
                codeVerifier.toByteArray(StandardCharsets.UTF_8).base64UrlEncodedString()

            return authApi.requestTokens(
                cookie = "connect.sid=$encodedSessionCode",
                body = mapOf("codeVerifier" to encodedVerifier)
            )
        } catch (e: Exception) {
            throw AuthenticationServiceError.Unknown
        }
    }

    override suspend fun refreshToken(refreshToken: String): TokenResponseDTO {
        val body = mapOf("refreshToken" to refreshToken)
        return authApi.refreshTokens(body = body)
    }
}