package com.sparcs.soap.Domain.Repositories.Feed

import com.sparcs.soap.Domain.Models.Feed.FeedUser
import com.sparcs.soap.Networking.RetrofitAPI.Feed.FeedUserApi
import com.sparcs.soap.Networking.RetrofitAPI.Feed.RegisterRequest
import javax.inject.Inject

interface FeedUserRepositoryProtocol {
    suspend fun register(ssoInfo: String)
    suspend fun getUser(): FeedUser
}

class FeedUserRepository @Inject constructor(
    private val api: FeedUserApi,
) : FeedUserRepositoryProtocol {

    override suspend fun register(ssoInfo: String) {
        val request = RegisterRequest(ssoInfo)
        api.register(request)
    }

    override suspend fun getUser(): FeedUser {
        val dto = api.getUser()
        return dto.toModel()
    }
}