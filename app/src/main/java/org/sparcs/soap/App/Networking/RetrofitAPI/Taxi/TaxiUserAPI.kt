package org.sparcs.soap.App.Networking.RetrofitAPI.Taxi

import org.sparcs.soap.App.Networking.ResponseDTO.Taxi.TaxiFavoriteRouteDTO
import org.sparcs.soap.App.Networking.ResponseDTO.Taxi.TaxiUserDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TaxiUserApi {

    @GET("logininfo")
    suspend fun fetchUserInfo(): Response<TaxiUserDTO>

    @POST("users/editBadge")
    suspend fun editBadge(
        @Body request: Map<String, String> //badge: ~~
    ): Response<Unit>

    @POST("users/editAccount")
    suspend fun editBankAccount(
        @Body request: Map<String, String> //account: ~~
    ): Response<Unit>

    @POST("users/registerPhoneNumber")
    suspend fun registerPhoneNumber(
        @Body request: Map<String, String> //phoneNumber: ~~
    ): Response<Unit>

    @POST("users/registerResidence")
    suspend fun registerResidence(
        @Body request: Map<String, String> //residence: ~~
    ): Response<Unit>

    @POST("users/deleteResidence")
    suspend fun deleteResidence()

    @GET("users/getFavorite")
    suspend fun getFavoriteRoutes(): Response<List<TaxiFavoriteRouteDTO>>

    @POST("users/createFavorite")
    suspend fun createFavorite(
        @Body request: Map<String, String> //from: ~~, to: ~~
    ): Response<Unit>

    @POST("users/deleteFavorite/{id}")
    suspend fun deleteFavorite(
        @Path("id") id: String
    ): Response<Unit>
}
