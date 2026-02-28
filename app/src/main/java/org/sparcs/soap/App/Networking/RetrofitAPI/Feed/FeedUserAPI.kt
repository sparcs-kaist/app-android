package org.sparcs.soap.App.Networking.RetrofitAPI.Feed

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import org.sparcs.soap.App.Networking.ResponseDTO.Feed.FeedUserDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

interface FeedUserApi {

    @POST("auth/bootstrap")
    suspend fun register(
        @Body request: RegisterRequest
    )

    @GET("me")
    suspend fun getUser(): FeedUserDTO

    @PATCH("me")
    suspend fun updateNickname(
        @Body request: Map<String, String>
    ): FeedUserDTO

    @GET("me/karma")
    suspend fun getKarma(): KarmaResponse

    @Multipart
    @POST("me/profile-image")
    suspend fun uploadProfileImage(
        @Part file: MultipartBody.Part
    ): ProfileImageUpdateResponse

    @DELETE("me/profile-image")
    suspend fun resetProfileImage(): ProfileImageUpdateResponse
}

data class RegisterRequest(
    @SerializedName("sso_info")
    val ssoInfo: String
)

data class KarmaResponse(
    @SerializedName("karma_total") val karmaTotal: Int
)

data class ProfileImageUpdateResponse(
    @SerializedName("id") val id: String,
    @SerializedName("s3_key") val s3Key: String,
    @SerializedName("url") val url: String? = null
)