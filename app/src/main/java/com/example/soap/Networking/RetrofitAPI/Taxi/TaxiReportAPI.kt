package com.example.soap.Networking.RetrofitAPI.Taxi

import com.example.soap.Networking.RequestDTO.Taxi.TaxiCreateReportRequestDTO
import com.example.soap.Networking.ResponseDTO.Taxi.TaxiMyReportsResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TaxiReportApi {
    @GET("reports/searchByUser")
    suspend fun fetchMyReports(): Response<TaxiMyReportsResponseDTO>

    @POST("reports/create")
    suspend fun createReport(
        @Body request: TaxiCreateReportRequestDTO
    ): Response<Unit>
}