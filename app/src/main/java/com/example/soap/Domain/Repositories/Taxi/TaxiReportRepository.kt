package com.example.soap.Domain.Repositories.Taxi

import com.example.soap.Domain.Models.Taxi.TaxiCreateReport
import com.example.soap.Features.Settings.Taxi.TaxiReports
import com.example.soap.Networking.RequestDTO.TaxiCreateReportRequestDTO
import com.example.soap.Networking.RetrofitAPI.Taxi.TaxiReportApi
import javax.inject.Inject

interface TaxiReportRepositoryProtocol {
    suspend fun fetchMyReports(): TaxiReports
    suspend fun createReport(report: TaxiCreateReport)
}

class TaxiReportRepository @Inject constructor(
    private val api: TaxiReportApi
) : TaxiReportRepositoryProtocol {

    override suspend fun fetchMyReports(): TaxiReports {
        try {
            val response = api.fetchMyReports()
            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Empty response body")
                val incoming = body.incoming.map { it.toModel() }
                val outgoing = body.outgoing.map { it.toModel() }
                return TaxiReports(incoming, outgoing)
            } else {
                val errorBody = response.errorBody()?.string()
                throw ApiException(errorBody ?: "Unknown error", response.code())
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun createReport(report: TaxiCreateReport) {
        try {
            val dto = TaxiCreateReportRequestDTO.fromModel(report)
            val response = api.createReport(dto)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                throw ApiException(errorBody ?: "Unknown error", response.code())
            }
        } catch (e: Exception) {
            throw e
        }
    }
}

class ApiException(message: String, val code: Int) : Exception(message)