package org.sparcs.App.Domain.Repositories.Feed

import com.google.gson.Gson
import org.sparcs.App.Domain.Models.Feed.FeedUser
import org.sparcs.App.Networking.ResponseDTO.handleApiError
import org.sparcs.App.Networking.RetrofitAPI.Feed.FeedUserApi
import org.sparcs.App.Networking.RetrofitAPI.Feed.RegisterRequest
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