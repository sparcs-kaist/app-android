package org.sparcs.App.Domain.Services

import androidx.activity.ComponentActivity
import org.sparcs.App.Networking.ResponseDTO.Auth.SignInResponseDTO
import org.sparcs.App.Networking.ResponseDTO.Auth.TokenResponseDTO

interface AuthenticationServiceProtocol {

    @Throws(Exception::class)
    suspend fun authenticate(activity: ComponentActivity): SignInResponseDTO

    @Throws(Exception::class)
    suspend fun refreshAccessToken(refreshToken: String): TokenResponseDTO
}