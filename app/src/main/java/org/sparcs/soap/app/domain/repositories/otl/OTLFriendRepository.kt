package org.sparcs.soap.app.domain.repositories.otl

import com.google.gson.Gson
import org.sparcs.soap.app.domain.models.otl.FriendList
import org.sparcs.soap.app.networking.requestDTO.otl.AddFriendRequestDTO
import org.sparcs.soap.app.networking.requestDTO.otl.SetFriendFavoriteRequestDTO
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.otl.OTLFriendApi
import javax.inject.Inject

interface OTLFriendRepositoryProtocol {
    suspend fun fetchFriends(): FriendList
    suspend fun addFriend(code: String)
    suspend fun deleteFriend(id: Int)
    suspend fun setFavorite(id: Int, isFavorite: Boolean)
    suspend fun fetchMyCode(): String
}

class OTLFriendRepository @Inject constructor(
    private val api: OTLFriendApi,
    private val gson: Gson = Gson(),
) : OTLFriendRepositoryProtocol {

    override suspend fun fetchFriends(): FriendList = safeApiCall(gson) {
        api.fetchFriends()
    }.toModel()

    override suspend fun addFriend(code: String) = safeApiCall(gson) {
        api.addFriend(AddFriendRequestDTO(code = code))
    }

    override suspend fun deleteFriend(id: Int) = safeApiCall(gson) {
        api.deleteFriend(id)
    }

    override suspend fun setFavorite(id: Int, isFavorite: Boolean) = safeApiCall(gson) {
        api.setFavorite(id, SetFriendFavoriteRequestDTO(isFavorite = isFavorite))
    }

    override suspend fun fetchMyCode(): String = safeApiCall(gson) {
        api.fetchMyCode()
    }.code
}
