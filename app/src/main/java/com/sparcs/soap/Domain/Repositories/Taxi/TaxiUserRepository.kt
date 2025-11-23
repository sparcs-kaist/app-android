package com.sparcs.soap.Domain.Repositories.Taxi

import com.google.gson.Gson
import com.sparcs.soap.Domain.Models.Taxi.TaxiUser
import com.sparcs.soap.Networking.ResponseDTO.handleApiError
import com.sparcs.soap.Networking.RetrofitAPI.Taxi.TaxiUserApi
import javax.inject.Inject

interface TaxiUserRepositoryProtocol {
    suspend fun fetchUser(): TaxiUser
    suspend fun editBankAccount(account: String)
}

enum class TaxiUserErrorCode(val code: Int) {
    EDIT_BANK_ACCOUNT_FAILED(2001)
}

class TaxiUserRepository @Inject constructor(
    private val api: TaxiUserApi,
    private val gson: Gson = Gson(),
) : TaxiUserRepositoryProtocol {
    override suspend fun fetchUser(): TaxiUser {
        try {
            val response = api.fetchUserInfo()
            val dto = response.body() ?: throw Exception("Empty response")
            return dto.toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun editBankAccount(account: String) {
        val response = api.editBankAccount(account)

        if (!response.isSuccessful) {
            throw Exception("Failed to edit bank account (code=${TaxiUserErrorCode.EDIT_BANK_ACCOUNT_FAILED.code})")
        }
    }
}