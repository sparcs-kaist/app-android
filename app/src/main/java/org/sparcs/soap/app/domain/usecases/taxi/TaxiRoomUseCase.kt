package org.sparcs.soap.app.domain.usecases.taxi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sparcs.soap.app.domain.enums.taxi.TaxiRoomBlockStatus
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.helpers.UserStorageProtocol
import org.sparcs.soap.app.domain.repositories.taxi.TaxiRoomRepositoryProtocol
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
        val taxiRooms = runCatching {
            taxiRoomRepository.fetchMyRooms().first
        }.getOrNull()

        if (taxiUser == null || taxiRooms == null) {
            return@withContext TaxiRoomBlockStatus.Error("Failed to load user information.")
        }

        if (!taxiUser.hasUserPaid(taxiRooms)) {
            return@withContext TaxiRoomBlockStatus.NotPaid
        }

        if (taxiRooms.size >= Constants.TAXI_MAX_ROOM_COUNT) {
            return@withContext TaxiRoomBlockStatus.TooManyRooms
        }

        return@withContext TaxiRoomBlockStatus.Allow
    }
}
