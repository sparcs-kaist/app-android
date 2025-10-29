package com.example.soap.Networking.RetrofitAPI.OTL

import com.example.soap.Networking.ResponseDTO.OTL.OTLUserDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OTLUserApi {

    @POST("session/register-oneapp")
    suspend fun register(
        @Body params: Map<String, String>
    ): Response<Unit>

    @GET("session/info")
    suspend fun fetchUserInfo(): Response<OTLUserDTO>
}