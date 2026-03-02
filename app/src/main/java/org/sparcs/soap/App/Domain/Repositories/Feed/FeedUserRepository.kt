package org.sparcs.soap.App.Domain.Repositories.Feed

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Models.Feed.FeedUser
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.Feed.FeedUserApi
import org.sparcs.soap.App.Networking.RetrofitAPI.Feed.RegisterRequest
import javax.inject.Inject

interface FeedUserRepositoryProtocol {
    suspend fun register(ssoInfo: String)
    suspend fun getUser(): FeedUser
    suspend fun getKarma(): Int
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

    override suspend fun getKarma(): Int = safeApiCall(gson) {
        api.getKarma()
    }.karmaTotal
}