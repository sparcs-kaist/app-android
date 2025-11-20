package com.sparcs.soap.Networking.RetrofitAPI.Feed

import com.sparcs.soap.Networking.ResponseDTO.Feed.FeedUserDTO
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FeedUserApi {

    @POST("auth/bootstrap")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<Unit>

    @GET("me")
    suspend fun getUser(): FeedUserDTO
}

data class RegisterRequest(
    @SerializedName("sso_info")
    val ssoInfo: String
)