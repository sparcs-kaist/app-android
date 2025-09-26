package com.example.soap.Domain.Repositories.Feed

import com.example.soap.Domain.Models.Feed.FeedUser
import com.example.soap.Networking.RetrofitAPI.Feed.FeedUserApi
import com.example.soap.Networking.RetrofitAPI.Feed.RegisterRequest

interface FeedUserRepositoryProtocol {
    suspend fun register(ssoInfo: String)
    suspend fun getUser(): FeedUser
}

class FeedUserRepository(
    private val api: FeedUserApi
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