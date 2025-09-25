package com.example.soap.Domain.Repositories.Taxi

import com.example.soap.Domain.Models.Taxi.TaxiUser
import com.example.soap.Networking.RetrofitAPI.Taxi.TaxiUserApi
import javax.inject.Inject

interface TaxiUserRepositoryProtocol {
    suspend fun fetchUser(): TaxiUser
    suspend fun editBankAccount(account: String)
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
}

class TaxiUserError(val code: Int, override val message: String) : Exception(message)
