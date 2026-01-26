package org.sparcs.soap.App.Domain.Repositories.Feed

import com.google.gson.Gson
import okhttp3.MultipartBody
import org.sparcs.soap.App.Domain.Models.Feed.FeedUser
import org.sparcs.soap.App.Networking.ResponseDTO.handleApiError
import org.sparcs.soap.App.Networking.RetrofitAPI.Feed.FeedUserApi
import org.sparcs.soap.App.Networking.RetrofitAPI.Feed.RegisterRequest
import javax.inject.Inject

interface FeedUserRepositoryProtocol {
    suspend fun register(ssoInfo: String)
    suspend fun getUser(): FeedUser
    suspend fun updateNickname(nickname: String): FeedUser
    suspend fun getKarma(): Int
    suspend fun uploadProfileImage(image: MultipartBody.Part): String
    suspend fun resetProfileImage()
}

class FeedUserRepository @Inject constructor(
    private val api: FeedUserApi,
    private val gson: Gson = Gson(),
) : FeedUserRepositoryProtocol {

    override suspend fun register(ssoInfo: String) {
        try {
            val request = RegisterRequest(ssoInfo)
            api.register(request)
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun getUser(): FeedUser = try {
        api.getUser().toModel()
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun updateNickname(nickname: String): FeedUser = try {
        api.updateNickname(mapOf("nickname" to nickname)).toModel()
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun getKarma(): Int = try {
        api.getKarma().karmaTotal
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun uploadProfileImage(image: MultipartBody.Part): String = try {
        api.uploadProfileImage(image).s3Key
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun resetProfileImage() {
        try {
            api.resetProfileImage()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }
}