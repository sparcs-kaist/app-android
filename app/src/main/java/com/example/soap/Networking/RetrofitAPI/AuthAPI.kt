package com.example.soap.Networking.RetrofitAPI

import com.example.soap.Networking.ResponseDTO.Auth.SignInResponseDTO
import com.example.soap.Networking.ResponseDTO.Auth.TokenResponseDTO
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/sparcsapp/token/issue")
    suspend fun requestTokens(
        @Header("Cookie") cookie: String,
        @Body body: Map<String, String>
    ): SignInResponseDTO

    @POST("auth/sparcsapp/token/refresh")
    suspend fun refreshTokens(
        @Body body: Map<String, String>
    ): TokenResponseDTO
}
