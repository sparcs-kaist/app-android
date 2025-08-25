package com.example.soap.Networking.RetrofitAPI.Taxi

import com.example.soap.Networking.RequestDTO.TaxiChatRequestDTO
import com.example.soap.Networking.ResponseDTO.Taxi.TaxiChatDTO
import com.example.soap.Networking.ResponseDTO.Taxi.TaxiChatPresignedURLDTO
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface TaxiChatApi {

    @POST("chats")
    suspend fun fetchChats(
        @Body body: Map<String, String> // {"roomId": roomID}
    ): Response<List<TaxiChatDTO>>

    @POST("chats/load/before")
    suspend fun fetchChatsBefore(
        @Body body: Map<String, String> // {"roomId": roomID, "lastMsgDate": date}
    ): Response<List<TaxiChatDTO>>

    @POST("chats/load/after")
    suspend fun fetchChatsAfter(
        @Body body: Map<String, String> // {"roomId": roomID, "lastMsgDate": date}
    ): Response<List<TaxiChatDTO>>

    @POST("chats/send")
    suspend fun sendChat(
        @Body request: TaxiChatRequestDTO
    ): Response<TaxiChatDTO>

    @POST("chats/read")
    suspend fun readChat(
        @Body body: Map<String, String> // {"roomId": roomID}
    ): Response<Unit>

    @POST("chats/uploadChatImg/getPUrl")
    suspend fun getPresignedURL(
        @Body body: Map<String, String> // {"roomId": roomID, "type": "image/png"}
    ): Response<TaxiChatPresignedURLDTO>

    @Multipart
    @POST
    suspend fun uploadImage(
        @Url url: String,
        @Part parts: List<MultipartBody.Part>
    ): Response<Unit>

    @POST("chats/uploadChatImg/done")
    suspend fun notifyImageUploadComplete(
        @Body body: Map<String, String> // {"id": id}
    ): Response<Unit>
}
