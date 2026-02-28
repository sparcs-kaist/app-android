package org.sparcs.soap.App.Domain.Repositories.OTL

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Models.OTL.OTLUser
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.OTLUserApi
import javax.inject.Inject

interface OTLUserRepositoryProtocol {
    suspend fun register(ssoInfo: String)
    suspend fun fetchUser(): OTLUser
}

class OTLUserRepository @Inject constructor(
    private val api: OTLUserApi,
    private val gson: Gson = Gson(),
) : OTLUserRepositoryProtocol {

    override suspend fun register(ssoInfo: String) = safeApiCall(gson) {
        api.register(mapOf("sso_info" to ssoInfo))
    }

    override suspend fun fetchUser(): OTLUser = safeApiCall(gson) {
        val response = api.fetchUserInfo()
        response.body() ?: throw Exception("Empty response")
    }.toModel()
}