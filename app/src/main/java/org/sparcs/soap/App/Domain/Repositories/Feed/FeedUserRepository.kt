package org.sparcs.soap.App.Domain.Repositories.Feed

import com.google.gson.Gson
import okhttp3.MultipartBody
import org.sparcs.soap.App.Domain.Models.Feed.FeedUser
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.Feed.FeedUserApi
import org.sparcs.soap.App.Networking.RetrofitAPI.Feed.RegisterRequest
import javax.inject.Inject

interface FeedUserRepositoryProtocol {
    suspend fun register(ssoInfo: String)
    suspend fun getUser(): FeedUser
    suspend fun updateNickname(nickname: String): FeedUser
    suspend fun getKarma(): Int
    suspend fun uploadProfileImage(image: MultipartBody.Part): String
    suspend fun resetProfileImage(): String
}

class FeedUserRepository @Inject constructor(
    private val api: FeedUserApi,
    private val gson: Gson = Gson(),
) : FeedUserRepositoryProtocol {

    override suspend fun register(ssoInfo: String) = safeApiCall(gson) {
        api.register(RegisterRequest(ssoInfo))
    }

    override suspend fun getUser(): FeedUser = safeApiCall(gson) {
        api.getUser()
    }.toModel()

    override suspend fun updateNickname(nickname: String): FeedUser = safeApiCall(gson) {
        api.updateNickname(mapOf("nickname" to nickname))
    }.toModel()

    override suspend fun getKarma(): Int = safeApiCall(gson) {
        api.getKarma()
    }.karmaTotal

    override suspend fun uploadProfileImage(image: MultipartBody.Part): String = safeApiCall(gson) {
        api.uploadProfileImage(image)
    }.s3Key

    override suspend fun resetProfileImage() = safeApiCall(gson) {
        api.resetProfileImage()
    }.s3Key
}