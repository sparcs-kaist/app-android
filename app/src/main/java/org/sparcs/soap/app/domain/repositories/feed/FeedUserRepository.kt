package org.sparcs.soap.app.domain.repositories.feed

import com.google.gson.Gson
import org.sparcs.soap.app.domain.models.feed.FeedUser
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.feed.FeedUserApi
import org.sparcs.soap.app.networking.retrofitAPI.feed.RegisterRequest
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