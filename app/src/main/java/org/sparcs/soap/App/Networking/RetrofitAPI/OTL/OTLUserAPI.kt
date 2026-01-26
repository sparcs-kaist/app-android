package org.sparcs.soap.App.Networking.RetrofitAPI.OTL

import org.sparcs.soap.App.Networking.ResponseDTO.OTL.OTLUserDTO
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