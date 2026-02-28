package org.sparcs.soap.App.Domain.Repositories.Taxi

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Enums.Taxi.TaxiReports
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiCreateReport
import org.sparcs.soap.App.Networking.RequestDTO.Taxi.TaxiCreateReportRequestDTO
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.Taxi.TaxiReportApi
import javax.inject.Inject

interface TaxiReportRepositoryProtocol {
    suspend fun fetchMyReports(): TaxiReports
    suspend fun createReport(report: TaxiCreateReport)
}

class TaxiReportRepository @Inject constructor(
    private val api: TaxiReportApi,
    private val gson: Gson = Gson(),
) : TaxiReportRepositoryProtocol {

    override suspend fun fetchMyReports(): TaxiReports = safeApiCall(gson) {
        val body = api.fetchMyReports()
        TaxiReports(
            incoming = body.incoming.map { it.toModel() },
            outgoing = body.outgoing.map { it.toModel() }
        )
    }

    override suspend fun createReport(report: TaxiCreateReport) = safeApiCall(gson) {
        api.createReport(TaxiCreateReportRequestDTO.fromModel(report))
    }
}