package com.sparcs.soap.Networking.RetrofitAPI.OTL

import com.sparcs.soap.Networking.ResponseDTO.OTL.OTLUserDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OTLUserApi {

    @POST("session/register-oneapp")
    suspend fun register(
        @Body params: Map<String, String>
    )

    @GET("session/info")
    suspend fun fetchUserInfo(): Response<OTLUserDTO>
}