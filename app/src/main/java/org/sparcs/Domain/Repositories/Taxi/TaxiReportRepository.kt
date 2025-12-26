package org.sparcs.Domain.Repositories.Taxi

import com.google.gson.Gson
import org.sparcs.Domain.Enums.Taxi.TaxiReports
import org.sparcs.Domain.Models.Taxi.TaxiCreateReport
import org.sparcs.Networking.RequestDTO.Taxi.TaxiCreateReportRequestDTO
import org.sparcs.Networking.ResponseDTO.handleApiError
import org.sparcs.Networking.RetrofitAPI.Taxi.TaxiReportApi
import javax.inject.Inject

interface TaxiReportRepositoryProtocol {
    suspend fun fetchMyReports(): TaxiReports
    suspend fun createReport(report: TaxiCreateReport)
}

class TaxiReportRepository @Inject constructor(
    private val api: TaxiReportApi,
    private val gson: Gson = Gson(),
) : TaxiReportRepositoryProtocol {

    override suspend fun fetchMyReports(): TaxiReports {
        try {
            val body = api.fetchMyReports()
            return TaxiReports(
                body.incoming.map { it.toModel() },
                body.outgoing.map { it.toModel() }
            )
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }


    override suspend fun createReport(report: TaxiCreateReport) {
        try {
            api.createReport(TaxiCreateReportRequestDTO.fromModel(report))
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }
}