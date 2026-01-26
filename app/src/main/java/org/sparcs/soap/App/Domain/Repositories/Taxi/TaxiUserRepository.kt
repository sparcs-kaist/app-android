package org.sparcs.soap.App.Domain.Repositories.Taxi

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.soap.App.Networking.ResponseDTO.handleApiError
import org.sparcs.soap.App.Networking.RetrofitAPI.Taxi.TaxiUserApi
import javax.inject.Inject

interface TaxiUserRepositoryProtocol {
    suspend fun fetchUser(): TaxiUser
    suspend fun editBadge(showBadge: Boolean)
    suspend fun editBankAccount(account: String)
    suspend fun registerPhoneNumber(phoneNumber: String)
    suspend fun registerResidence(residence: String)
}

enum class TaxiUserErrorCode(val code: Int) {
    EDIT_BANK_ACCOUNT_FAILED(2001),
    EDIT_BADGE_FAILED(2002),
    REGISTER_PHONE_NUMBER_FAILED(2003),
    REGISTER_RESIDENCE_FAILED(2004)
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

    override suspend fun editBadge(showBadge: Boolean) {
        val response = api.editBadge(mapOf("badge" to showBadge.toString()))

        if (!response.isSuccessful) {
            throw Exception("Failed to edit badge (code=${TaxiUserErrorCode.EDIT_BADGE_FAILED.code})")
        }
    }

    override suspend fun editBankAccount(account: String) {
        val response = api.editBankAccount(mapOf("account" to account))

        if (!response.isSuccessful) {
            throw Exception("Failed to edit bank account (code=${TaxiUserErrorCode.EDIT_BANK_ACCOUNT_FAILED.code})")
        }
    }

    override suspend fun registerPhoneNumber(phoneNumber: String) {
        val response = api.registerPhoneNumber(mapOf("phoneNumber" to phoneNumber))

        if (!response.isSuccessful) {
            throw Exception("Failed to register phone number (code=${TaxiUserErrorCode.REGISTER_PHONE_NUMBER_FAILED.code})")
        }
    }

    override suspend fun registerResidence(residence: String) {
        if(residence.isEmpty()){ api.deleteResidence() } else {
            val response = api.registerResidence(mapOf("residence" to residence))

            if (!response.isSuccessful) {
                throw Exception("Failed to register residence (code=${TaxiUserErrorCode.REGISTER_RESIDENCE_FAILED.code})")
            }
        }
    }
}