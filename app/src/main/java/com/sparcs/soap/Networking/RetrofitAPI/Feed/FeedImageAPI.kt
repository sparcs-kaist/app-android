package com.sparcs.soap.Networking.RetrofitAPI.Feed

import com.sparcs.soap.Networking.ResponseDTO.Feed.FeedImageDTO
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface FeedImageApi {
    @Multipart
    @POST("images")
    suspend fun uploadPostImage(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("spoiler") spoiler: RequestBody
    ): FeedImageDTO
}