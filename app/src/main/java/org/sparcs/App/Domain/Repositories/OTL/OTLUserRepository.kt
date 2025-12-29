package org.sparcs.App.Domain.Repositories.OTL

import com.google.gson.Gson
import org.sparcs.App.Domain.Models.OTL.OTLUser
import org.sparcs.App.Networking.ResponseDTO.handleApiError
import org.sparcs.App.Networking.RetrofitAPI.OTL.OTLUserApi
import javax.inject.Inject

interface OTLUserRepositoryProtocol {
    suspend fun register(ssoInfo: String)
    suspend fun fetchUser(): OTLUser
}

class OTLUserRepository @Inject constructor(
    private val api: OTLUserApi,
    private val gson: Gson = Gson(),
) : OTLUserRepositoryProtocol {

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