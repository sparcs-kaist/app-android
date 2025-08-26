package com.example.soap.Domain.Services

import android.app.Activity
import com.example.soap.Networking.ResponseDTO.Auth.SignInResponseDTO
import com.example.soap.Networking.ResponseDTO.Auth.TokenResponseDTO

interface AuthenticationServiceProtocol {

    @Throws(Exception::class)
    suspend fun authenticate(activity: Activity): SignInResponseDTO

    @Throws(Exception::class)
    suspend fun refreshAccessToken(refreshToken: String): TokenResponseDTO
}