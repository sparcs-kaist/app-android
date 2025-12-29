package org.sparcs.App.Domain.Usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sparcs.App.Domain.Enums.Taxi.TaxiRoomBlockStatus
import org.sparcs.App.Domain.Helpers.Constants
import org.sparcs.App.Domain.Helpers.UserStorageProtocol
import org.sparcs.App.Domain.Repositories.Taxi.TaxiRoomRepositoryProtocol
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
