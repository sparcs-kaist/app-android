package org.sparcs.soap.app.domain.repositories.ara

import com.google.gson.Gson
import org.sparcs.soap.app.domain.models.ara.AraBlockedUser
import org.sparcs.soap.app.domain.models.ara.AraUser
import org.sparcs.soap.app.networking.responseDTO.ara.AraSignInResponseDTO
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.ara.AraUserApi
import retrofit2.HttpException
import javax.inject.Inject

interface AraUserRepositoryProtocol {
    suspend fun register(ssoInfo: String): AraSignInResponseDTO
    suspend fun agreeTOS(userID: Int)
    suspend fun fetchUser(): AraUser
    suspend fun updateMe(id: Int, params: Map<String, Any>)
    suspend fun blockUser(userID: Int)
    suspend fun unblockUser(userID: Int)
    suspend fun fetchBlockedUsers(): List<AraBlockedUser>
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

    override suspend fun blockUser(userID: Int) = safeApiCall(gson) {
        val response = api.block(mapOf("user" to userID))
        if (!response.isSuccessful) throw HttpException(response)
    }

    override suspend fun unblockUser(userID: Int) = safeApiCall(gson) {
        val response = api.unblock(mapOf("blocked" to userID))
        if (!response.isSuccessful) throw HttpException(response)
    }

    override suspend fun fetchBlockedUsers(): List<AraBlockedUser> = safeApiCall(gson) {
        buildList {
            var page = 1
            while (true) {
                val blocks = api.fetchBlocks(page = page, pageSize = BLOCKS_PAGE_SIZE)
                addAll(blocks.results.mapNotNull { it.toModel() })
                if (blocks.current >= blocks.numPages) break
                page++
            }
        }
    }

    private companion object {
        const val BLOCKS_PAGE_SIZE = 100
    }
}