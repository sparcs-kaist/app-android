package com.sparcs.soap.Networking.RetrofitAPI.Taxi

import com.sparcs.soap.Networking.RequestDTO.Taxi.TaxiCreateReportRequestDTO
import com.sparcs.soap.Networking.ResponseDTO.Taxi.TaxiMyReportsResponseDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TaxiReportApi {
    @GET("reports/searchByUser")
    suspend fun fetchMyReports(): TaxiMyReportsResponseDTO

    @POST("reports/create")
    suspend fun createReport(
        @Body request: TaxiCreateReportRequestDTO
    )
}