package com.example.soap.Domain.Repositories

import com.example.soap.Domain.Models.Taxi.TaxiReport
import com.example.soap.Domain.Models.Taxi.TaxiUser
import com.example.soap.Features.Settings.Taxi.TaxiReports
import com.example.soap.Networking.RetrofitAPI.Taxi.TaxiUserApi
import javax.inject.Inject

interface TaxiUserRepositoryProtocol {
    suspend fun fetchUser(): TaxiUser
    suspend fun editBankAccount(account: String)
    suspend fun fetchReports(): TaxiReports
}

enum class TaxiUserErrorCode(val code: Int) {
    EDIT_BANK_ACCOUNT_FAILED(2001)
}

class TaxiUserRepository @Inject constructor(
    private val taxiUserApi: TaxiUserApi
) : TaxiUserRepositoryProtocol {

    override suspend fun fetchUser(): TaxiUser {
        val response = taxiUserApi.fetchUserInfo()
        if (!response.isSuccessful) {
            throw Exception("Failed to fetch user: ${response.code()}")
        }
        val dto = response.body() ?: throw Exception("Empty response")
        return dto.toModel()
    }

    override suspend fun editBankAccount(account: String) {
        val response = taxiUserApi.editBankAccount(account)
        if (!response.isSuccessful) {
            throw Exception(
                "Failed to edit bank account",
                Throwable().apply {
                    initCause(
                        TaxiUserError(
                            TaxiUserErrorCode.EDIT_BANK_ACCOUNT_FAILED.code,
                            "Failed to edit bank account"
                        )
                    )
                }
            )
        }
    }

    override suspend fun fetchReports(): TaxiReports{
        val response = taxiUserApi.fetchReports()
        if (!response.isSuccessful) {
            throw Exception("Failed to fetch reports: ${response.code()}")
        }
        val dto = response.body() ?: throw Exception("Empty response")

        val reported = dto.reported.map { it.toModel(TaxiReport.ReportType.REPORTED) }
        val reporting = dto.reporting.map { it.toModel(TaxiReport.ReportType.REPORTING) }

        return TaxiReports(reported, reporting)
    }
}

class TaxiUserError(val code: Int, override val message: String) : Exception(message)
