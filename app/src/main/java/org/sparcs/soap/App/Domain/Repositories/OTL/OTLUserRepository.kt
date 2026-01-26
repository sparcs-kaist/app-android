package org.sparcs.soap.App.Domain.Repositories.OTL

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Models.OTL.OTLUser
import org.sparcs.soap.App.Networking.ResponseDTO.handleApiError
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.OTLUserApi
import javax.inject.Inject

interface OTLUserRepositoryProtocol {
    suspend fun register(ssoInfo: String)
    suspend fun fetchUser(): OTLUser
}

class OTLUserRepository @Inject constructor(
    private val api: OTLUserApi,
    private val gson: Gson = Gson(),
) : org.sparcs.soap.App.Domain.Repositories.OTL.OTLUserRepositoryProtocol {

    override suspend fun register(ssoInfo: String) = try {
        api.register(
            mapOf("sso_info" to ssoInfo)
        )
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun fetchUser(): OTLUser {
        try {
            val response = api.fetchUserInfo()
            val dto = response.body() ?: throw Exception("Empty response")
            return dto.toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }
}