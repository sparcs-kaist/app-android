package org.sparcs.soap.app.domain.repositories.ara

import com.google.gson.Gson
import org.sparcs.soap.app.domain.models.ara.AraUser
import org.sparcs.soap.app.networking.responseDTO.ara.AraSignInResponseDTO
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.ara.AraUserApi
import javax.inject.Inject

interface AraUserRepositoryProtocol {
    suspend fun register(ssoInfo: String): AraSignInResponseDTO
    suspend fun agreeTOS(userID: Int)
    suspend fun fetchUser(): AraUser
    suspend fun updateMe(id: Int, params: Map<String, Any>)
}

class AraUserRepository @Inject constructor(
    private val api: AraUserApi,
    private val gson: Gson = Gson(),
) : AraUserRepositoryProtocol {

    override suspend fun register(ssoInfo: String): AraSignInResponseDTO = safeApiCall(gson) {
        api.register(mapOf("ssoInfo" to ssoInfo))
    }

    override suspend fun agreeTOS(userID: Int) = safeApiCall(gson) {
        api.agreeTOS(userID)
    }

    override suspend fun fetchUser(): AraUser = safeApiCall(gson) {
        api.fetchMe()
    }.toModel()

    override suspend fun updateMe(id: Int, params: Map<String, Any>) = safeApiCall(gson) {
        api.updateUser(id, params)
    }
}