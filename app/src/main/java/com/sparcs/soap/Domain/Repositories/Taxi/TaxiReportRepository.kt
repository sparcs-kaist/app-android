package com.sparcs.soap.Domain.Repositories.Taxi

import com.google.gson.Gson
import com.sparcs.soap.Domain.Enums.Taxi.TaxiReports
import com.sparcs.soap.Domain.Models.Taxi.TaxiCreateReport
import com.sparcs.soap.Networking.RequestDTO.Taxi.TaxiCreateReportRequestDTO
import com.sparcs.soap.Networking.ResponseDTO.handleApiError
import com.sparcs.soap.Networking.RetrofitAPI.Taxi.TaxiReportApi
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