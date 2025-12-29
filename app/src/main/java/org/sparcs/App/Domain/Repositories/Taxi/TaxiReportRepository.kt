package org.sparcs.App.Domain.Repositories.Taxi

import com.google.gson.Gson
import org.sparcs.App.Domain.Enums.Taxi.TaxiReports
import org.sparcs.App.Domain.Models.Taxi.TaxiCreateReport
import org.sparcs.App.Networking.RequestDTO.Taxi.TaxiCreateReportRequestDTO
import org.sparcs.App.Networking.ResponseDTO.handleApiError
import org.sparcs.App.Networking.RetrofitAPI.Taxi.TaxiReportApi
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