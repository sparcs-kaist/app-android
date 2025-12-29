package org.sparcs.App.Domain.Repositories.Ara

import com.google.gson.Gson
import org.sparcs.App.Domain.Models.Ara.AraUser
import org.sparcs.App.Networking.ResponseDTO.Ara.AraSignInResponseDTO
import org.sparcs.App.Networking.ResponseDTO.handleApiError
import org.sparcs.App.Networking.RetrofitAPI.Ara.AraUserApi
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

    override suspend fun register(ssoInfo: String): AraSignInResponseDTO = try {
        api.register(mapOf("ssoInfo" to ssoInfo))
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun agreeTOS(userID: Int) = try {
        api.agreeTOS(userID)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun fetchUser(): AraUser = try {
        api.fetchMe().toModel()
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun updateMe(id: Int, params: Map<String, Any>) = try {
        api.updateUser(id, params)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }
}