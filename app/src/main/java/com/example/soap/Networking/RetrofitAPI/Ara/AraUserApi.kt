package com.example.soap.Networking.RetrofitAPI.Ara


import com.example.soap.Networking.ResponseDTO.Ara.AraSignInResponseDTO
import com.example.soap.Networking.ResponseDTO.Ara.AraUserDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AraUserApi {

    @POST("users/oneapp-login/")
    suspend fun register(
        @Body body: Map<String, String>
    ): Response<AraSignInResponseDTO>

    @PATCH("user_profiles/{id}/agree_terms_of_service/")
    suspend fun agreeTOS(
        @Path("id") userID: Int
    ): Response<Unit>

    @GET("me")
    suspend fun fetchMe(): Response<AraUserDTO>

    @PATCH("user_profiles/{id}/")
    suspend fun updateUser(
        @Path("id") userID: Int,
        @Body params: Map<String, Any>
    ): Response<Unit>
}
