package org.sparcs.soap.App.Domain.Repositories.Feed

import com.google.gson.Gson
import okhttp3.MultipartBody
import org.sparcs.soap.App.Domain.Models.Feed.FeedUser
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.Feed.FeedProfileApi
import javax.inject.Inject

interface FeedProfileRepositoryProtocol {
    suspend fun updateNickname(nickname: String): FeedUser
    suspend fun setProfileImage(image: MultipartBody.Part): String
    suspend fun removeProfileImage(): String
}

class FeedProfileRepository @Inject constructor(
    private val api: FeedProfileApi,
    private val gson: Gson = Gson(),
) : FeedProfileRepositoryProtocol {

    override suspend fun updateNickname(nickname: String): FeedUser = safeApiCall(gson) {
        api.updateNickname(mapOf("nickname" to nickname))
    }.toModel()

    override suspend fun setProfileImage(image: MultipartBody.Part): String = safeApiCall(gson) {
        api.setProfileImage(image)
    }.url

    override suspend fun removeProfileImage() = safeApiCall(gson) {
        api.removeProfileImage()
    }.url
}

