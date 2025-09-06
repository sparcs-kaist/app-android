package com.example.soap.Networking.RetrofitAPI.Ara

import com.example.soap.Networking.ResponseDTO.Ara.AraSignInResponseDTO
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path


interface AraUserApi {
    @POST("users/oneapp-login/")
    suspend fun register(@Body body: String): AraSignInResponseDTO

    @PATCH("user_profiles/{id}/agree_terms_of_service/")
    suspend fun agreeTOS(@Path("id") userID: Int)
}
