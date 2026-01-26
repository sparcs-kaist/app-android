package org.sparcs.soap.App.Domain.Services

import androidx.activity.ComponentActivity
import org.sparcs.soap.App.Networking.ResponseDTO.Auth.SignInResponseDTO
import org.sparcs.soap.App.Networking.ResponseDTO.Auth.TokenResponseDTO

interface AuthenticationServiceProtocol {

    @Throws(Exception::class)
    suspend fun authenticate(activity: ComponentActivity): SignInResponseDTO

    @Throws(Exception::class)
    suspend fun refreshAccessToken(refreshToken: String): TokenResponseDTO
}