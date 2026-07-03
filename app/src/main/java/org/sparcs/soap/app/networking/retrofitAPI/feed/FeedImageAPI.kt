package org.sparcs.soap.app.networking.retrofitAPI.feed

import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.sparcs.soap.app.networking.responseDTO.feed.FeedImageDTO
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