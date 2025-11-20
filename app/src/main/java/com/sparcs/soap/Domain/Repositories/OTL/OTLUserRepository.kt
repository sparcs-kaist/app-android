package com.sparcs.soap.Domain.Repositories.OTL

import com.sparcs.soap.Domain.Models.OTL.OTLUser
import com.sparcs.soap.Networking.RetrofitAPI.OTL.OTLUserApi
import javax.inject.Inject

interface OTLUserRepositoryProtocol {
    suspend fun register(ssoInfo: String)
    suspend fun fetchUser(): OTLUser
}

class OTLUserRepository @Inject constructor(
    private val api: OTLUserApi
) : OTLUserRepositoryProtocol {

    override suspend fun register(ssoInfo: String) {
        val response = api.register(
            mapOf("sso_info" to ssoInfo)
        )
        if (!response.isSuccessful) {
            throw Exception("Register failed: ${response.code()}")
        }
    }

    override suspend fun fetchUser(): OTLUser {
        val response = api.fetchUserInfo()
        if (!response.isSuccessful) {
            throw Exception("Fetch user failed: ${response.code()}")
        }
        val dto = response.body() ?: throw Exception("Empty response")
        return dto.toModel()
    }
}