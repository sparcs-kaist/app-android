package org.sparcs.soap.app.networking.retrofitAPI.taxi

import org.sparcs.soap.app.networking.requestDTO.taxi.TaxiCreateReportRequestDTO
import org.sparcs.soap.app.networking.responseDTO.taxi.TaxiMyReportsResponseDTO
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