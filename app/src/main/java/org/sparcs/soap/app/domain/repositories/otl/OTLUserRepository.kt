package org.sparcs.soap.app.domain.repositories.otl

import com.google.gson.Gson
import org.sparcs.soap.app.domain.models.otl.OTLUser
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.otl.OTLUserApi
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