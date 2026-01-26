package org.sparcs.soap.App.Domain.Repositories.Taxi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChatRequest
import org.sparcs.soap.App.Networking.RequestDTO.Taxi.TaxiChatRequestDTO
import org.sparcs.soap.App.Networking.ResponseDTO.Taxi.TaxiChatPresignedURLDTO
import org.sparcs.soap.App.Networking.RetrofitAPI.Taxi.TaxiChatApi
import org.sparcs.soap.App.Shared.Extensions.toISO8601
import java.util.Date
import javax.inject.Inject

interface TaxiChatRepositoryProtocol {
    suspend fun fetchChats(roomID: String)
    suspend fun fetchChatsBefore(roomID: String, date: Date)
    suspend fun fetchChatsAfter(roomID: String, date: Date)
    suspend fun sendChat(chat: TaxiChatRequest)
    suspend fun readChats(roomID: String)
    suspend fun getPresignedURL(roomID: String): TaxiChatPresignedURLDTO
    suspend fun uploadImage(presignedURL: TaxiChatPresignedURLDTO, imageData: ByteArray)
    suspend fun notifyImageUploadComplete(id: String)
}

sealed class TaxiChatError(val code: Int, message: String) : Exception(message) {
    data object FetchChatsFailed : TaxiChatError(1001, "Failed to fetch chats") {
        private fun readResolve(): Any = FetchChatsFailed
    }

    data object SendChatFailed : TaxiChatError(1002, "Failed to send chat") {
        private fun readResolve(): Any = SendChatFailed
    }

    data object ReadChatFailed : TaxiChatError(1003, "Failed to read chats") {
        private fun readResolve(): Any = ReadChatFailed
    }
}

class TaxiChatRepository @Inject constructor(
    private val taxiChatApi: TaxiChatApi,
) : TaxiChatRepositoryProtocol {

    override suspend fun fetchChats(roomID: String) {
        val body = mapOf("roomId" to roomID)
        val result = taxiChatApi.fetchChats(body)
        if (!result.result) throw TaxiChatError.FetchChatsFailed
    }

    override suspend fun fetchChatsBefore(roomID: String, date: Date) {
        val body = mapOf("roomId" to roomID, "lastMsgDate" to date.toISO8601())
        val result = taxiChatApi.fetchChatsBefore(body)
        if (!result.result) throw TaxiChatError.FetchChatsFailed
    }

    override suspend fun fetchChatsAfter(roomID: String, date: Date) {
        val body = mapOf("roomId" to roomID, "lastMsgDate" to date.toISO8601())
        val result = taxiChatApi.fetchChatsAfter(body)
        if (!result.result) throw TaxiChatError.FetchChatsFailed
    }

    override suspend fun sendChat(chat: TaxiChatRequest) {
        val dto = TaxiChatRequestDTO.fromModel(chat)
        val result = taxiChatApi.sendChat(dto)
        if (!result.result) throw TaxiChatError.SendChatFailed
    }

    override suspend fun readChats(roomID: String) {
        val body = mapOf("roomId" to roomID)
        val result = taxiChatApi.readChat(body)
        if (!result.result) throw TaxiChatError.ReadChatFailed
    }
    override suspend fun getPresignedURL(roomID: String): TaxiChatPresignedURLDTO {
        val body = mapOf("roomId" to roomID, "type" to "image/png")
        return taxiChatApi.getPresignedURL(body)
    }

    override suspend fun uploadImage(presignedURL: TaxiChatPresignedURLDTO, imageData: ByteArray) {
        withContext(Dispatchers.IO) {
            val requestBody = imageData.toRequestBody("image/png".toMediaTypeOrNull())

            val request = Request.Builder()
                .url(presignedURL.url)
                .put(requestBody)
                .build()

            OkHttpClient().newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("S3 upload failed: ${response.code}")
            }
        }
    }


    override suspend fun notifyImageUploadComplete(id: String) {
        val body = mapOf("id" to id)
        taxiChatApi.notifyImageUploadComplete(body)
    }
}