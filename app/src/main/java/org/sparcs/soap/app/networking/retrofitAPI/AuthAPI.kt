package org.sparcs.soap.app.networking.retrofitAPI

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.networking.responseDTO.auth.SignInResponseDTO
import org.sparcs.soap.app.networking.responseDTO.auth.TokenResponseDTO
import retrofit2.http.Body
import retrofit2.http.POST

data class TokenIssueRequest(
    @SerializedName("session")
    val session: String,
    @SerializedName("codeVerifier")
    val codeVerifier: String
)

data class TokenRefreshRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)

data class LogoutRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)

interface AuthApi {

    @POST("auth/token/issue")
    suspend fun requestTokens(
        @Body body: TokenIssueRequest
    ): SignInResponseDTO

    @POST("auth/token/refresh")
    suspend fun refreshTokens(
        @Body body: TokenRefreshRequest
    ): TokenResponseDTO

    @POST("auth/logout")
    suspend fun logout(
        @Body body: LogoutRequest
    )
}
