package org.sparcs.soap.App.Networking.RetrofitAPI.Feed

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Networking.ResponseDTO.Feed.FeedUserDTO
import org.sparcs.soap.App.Networking.ResponseDTO.Feed.KarmaResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FeedUserApi {

    @POST("auth/bootstrap")
    suspend fun register(
        @Body request: RegisterRequest
    )

    @GET("me")
    suspend fun getUser(): FeedUserDTO

    @GET("me/karma")
    suspend fun getKarma(): KarmaResponse
}

data class RegisterRequest(
    @SerializedName("sso_info")
    val ssoInfo: String
)
