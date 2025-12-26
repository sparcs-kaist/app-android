package org.sparcs.Networking.RetrofitAPI.Taxi

import org.sparcs.Networking.RequestDTO.Taxi.TaxiCreateReportRequestDTO
import org.sparcs.Networking.ResponseDTO.Taxi.TaxiMyReportsResponseDTO
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