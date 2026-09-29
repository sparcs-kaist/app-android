package org.sparcs.soap.app.networking.retrofitAPI.otl

import org.sparcs.soap.app.networking.requestDTO.otl.AddFriendRequestDTO
import org.sparcs.soap.app.networking.requestDTO.otl.SetFriendFavoriteRequestDTO
import org.sparcs.soap.app.networking.responseDTO.otl.FriendCodeResponseDTO
import org.sparcs.soap.app.networking.responseDTO.otl.FriendListResponseDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface OTLFriendApi {
    @GET("api/v2/friends")
    suspend fun fetchFriends(): FriendListResponseDTO

    @POST("api/v2/friends")
    suspend fun addFriend(@Body request: AddFriendRequestDTO)

    @DELETE("api/v2/friends/{friendId}")
    suspend fun deleteFriend(@Path("friendId") friendID: Int)

    @PATCH("api/v2/friends/{friendId}/favorite")
    suspend fun setFavorite(
        @Path("friendId") friendID: Int,
        @Body request: SetFriendFavoriteRequestDTO,
    )

    @GET("api/v2/friends/code")
    suspend fun fetchMyCode(): FriendCodeResponseDTO
}
