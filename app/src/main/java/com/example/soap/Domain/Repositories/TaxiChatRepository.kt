package com.example.soap.Domain.Repositories

import com.example.soap.Domain.Models.Taxi.TaxiChatRequest
import com.example.soap.Networking.RequestDTO.TaxiChatRequestDTO
import com.example.soap.Networking.ResponseDTO.Taxi.TaxiChatPresignedURLDTO
import com.example.soap.Networking.RetrofitAPI.Taxi.TaxiChatApi
import com.example.soap.Shared.Extensions.toISO8601
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
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
    private val taxiChatApi: TaxiChatApi
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
            val parts = mutableListOf<MultipartBody.Part>()

            presignedURL.fields.forEach { (key, value) ->
                val part = MultipartBody.Part.createFormData(key, value)
                parts.add(part)
            }

            val imagePart = MultipartBody.Part.createFormData(
                "file",
                "blob.png",
                imageData.toRequestBody("image/png".toMediaTypeOrNull())
            )
            parts.add(imagePart)

            taxiChatApi.uploadImage(presignedURL.url, parts)
        }
    }

    override suspend fun notifyImageUploadComplete(id: String) {
        val body = mapOf("id" to id)
        taxiChatApi.notifyImageUploadComplete(body)
    }
}