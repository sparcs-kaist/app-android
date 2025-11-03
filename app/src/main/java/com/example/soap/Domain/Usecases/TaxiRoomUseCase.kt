package com.example.soap.Domain.Usecases

import com.example.soap.Domain.Enums.TaxiRoomBlockStatus
import com.example.soap.Domain.Helpers.Constants
import com.example.soap.Domain.Helpers.UserStorageProtocol
import com.example.soap.Domain.Repositories.Taxi.TaxiRoomRepositoryProtocol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface TaxiRoomUseCaseProtocol {
    suspend fun isBlocked(): TaxiRoomBlockStatus
}

class TaxiRoomUseCase @Inject constructor(
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol,
    private val userStorage: UserStorageProtocol
) : TaxiRoomUseCaseProtocol {

    // MARK: - Functions
    override suspend fun isBlocked(): TaxiRoomBlockStatus = withContext(Dispatchers.IO) {
        val taxiUser = userStorage.getTaxiUser()
        val taxiRooms = try {
            taxiRoomRepository.fetchMyRooms().first
        } catch (e: Exception) {
            null
        }

        if (taxiUser == null || taxiRooms == null) {
            return@withContext TaxiRoomBlockStatus.Error("Failed to load user information.")
        }

        if (!taxiUser.hasUserPaid(taxiRooms)) {
            return@withContext TaxiRoomBlockStatus.NotPaid
        }

        if (taxiRooms.size >= Constants.taxiMaxRoomCount) {
            return@withContext TaxiRoomBlockStatus.TooManyRooms
        }

        return@withContext TaxiRoomBlockStatus.Allow
    }
}
