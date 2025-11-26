package com.sparcs.soap.Domain.Services

import androidx.activity.ComponentActivity
import com.sparcs.soap.Networking.ResponseDTO.Auth.SignInResponseDTO
import com.sparcs.soap.Networking.ResponseDTO.Auth.TokenResponseDTO

interface AuthenticationServiceProtocol {

    @Throws(Exception::class)
    suspend fun authenticate(activity: ComponentActivity): SignInResponseDTO

    @Throws(Exception::class)
    suspend fun refreshAccessToken(refreshToken: String): TokenResponseDTO
}