package org.sparcs.soap.App.Networking.RetrofitAPI.Feed

import okhttp3.MultipartBody
import org.sparcs.soap.App.Networking.ResponseDTO.Feed.FeedProfileImageDTO
import org.sparcs.soap.App.Networking.ResponseDTO.Feed.FeedUserDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

interface FeedProfileApi {
    @PATCH("me")
    suspend fun updateNickname(
        @Body request: Map<String, String>
    ): FeedUserDTO

    @Multipart
    @POST("me/profile-image")
    suspend fun setProfileImage(
        @Part file: MultipartBody.Part
    ): FeedProfileImageDTO

    @DELETE("me/profile-image")
    suspend fun removeProfileImage(): FeedProfileImageDTO
}