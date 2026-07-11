package org.sparcs.soap.buddyTestSupport.repository

import org.sparcs.soap.app.domain.models.taxi.TaxiCreateRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.repositories.taxi.TaxiRoomRepositoryProtocol
import org.sparcs.soap.app.shared.mocks.taxi.mock

class MockTaxiRoomRepository : TaxiRoomRepositoryProtocol {

    var fetchRoomsResult: Result<List<TaxiRoom>> = Result.success(emptyList())
    var fetchMyRoomsResult: Result<Pair<List<TaxiRoom>, List<TaxiRoom>>> =
        Result.success(emptyList<TaxiRoom>() to emptyList())
    var fetchLocationsResult: Result<List<TaxiLocation>> = Result.success(emptyList())
    var roomActionResult: Result<TaxiRoom> = Result.success(TaxiRoom.mock())

    override suspend fun fetchRooms(): List<TaxiRoom> = fetchRoomsResult.getOrThrow()
    override suspend fun fetchMyRooms(): Pair<List<TaxiRoom>, List<TaxiRoom>> =
        fetchMyRoomsResult.getOrThrow()

    override suspend fun fetchLocations(): List<TaxiLocation> = fetchLocationsResult.getOrThrow()
    override suspend fun createRoom(with: TaxiCreateRoom): TaxiRoom = roomActionResult.getOrThrow()
    override suspend fun joinRoom(id: String): TaxiRoom = roomActionResult.getOrThrow()
    override suspend fun leaveRoom(id: String): TaxiRoom = roomActionResult.getOrThrow()
    override suspend fun getRoom(id: String): TaxiRoom = roomActionResult.getOrThrow()
    override suspend fun getPublicRoom(id: String): TaxiRoom = roomActionResult.getOrThrow()
    override suspend fun commitSettlement(id: String, amount: Int): TaxiRoom =
        roomActionResult.getOrThrow()

    override suspend fun commitPayment(id: String): TaxiRoom = roomActionResult.getOrThrow()
    override suspend fun toggleCarrier(id: String, hasCarrier: Boolean): TaxiRoom =
        roomActionResult.getOrThrow()

    override suspend fun updateArrival(id: String, isArrived: Boolean): TaxiRoom =
        roomActionResult.getOrThrow()
}
