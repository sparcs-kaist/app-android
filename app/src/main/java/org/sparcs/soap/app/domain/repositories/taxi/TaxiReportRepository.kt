package org.sparcs.soap.app.domain.repositories.taxi

import com.google.gson.Gson
import org.sparcs.soap.app.domain.enums.taxi.TaxiReports
import org.sparcs.soap.app.domain.models.taxi.TaxiCreateReport
import org.sparcs.soap.app.networking.requestDTO.taxi.TaxiCreateReportRequestDTO
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.taxi.TaxiReportApi
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