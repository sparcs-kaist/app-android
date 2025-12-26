package org.sparcs.Domain.Repositories.Feed

import com.google.gson.Gson
import org.sparcs.Domain.Models.Feed.FeedUser
import org.sparcs.Networking.ResponseDTO.handleApiError
import org.sparcs.Networking.RetrofitAPI.Feed.FeedUserApi
import org.sparcs.Networking.RetrofitAPI.Feed.RegisterRequest
import javax.inject.Inject

interface FeedUserRepositoryProtocol {
    suspend fun register(ssoInfo: String)
    suspend fun getUser(): FeedUser
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
}