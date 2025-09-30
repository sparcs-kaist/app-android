package com.example.soap.Networking.RetrofitAPI.Taxi

import com.example.soap.Networking.ResponseDTO.Taxi.TaxiMyReportsResponseDTO
import com.example.soap.Networking.ResponseDTO.Taxi.TaxiUserDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TaxiUserApi {

    @GET("logininfo")
    suspend fun fetchUserInfo(): Response<TaxiUserDTO>

    @POST("users/editAccount")
    suspend fun editBankAccount(
        @Body request: String
    ): Response<Unit>

    @GET("reports/searchByUser")
    suspend fun fetchReports(): TaxiMyReportsResponseDTO
}
