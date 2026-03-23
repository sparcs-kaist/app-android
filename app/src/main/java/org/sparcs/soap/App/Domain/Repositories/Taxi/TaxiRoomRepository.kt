package org.sparcs.soap.App.Domain.Repositories.Taxi

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiCreateRoom
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiLocation
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Networking.RequestDTO.Taxi.TaxiCreateRoomRequestDTO
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.Taxi.TaxiRoomApi
import org.sparcs.soap.App.Shared.Mocks.Taxi.mock
import org.sparcs.soap.App.Shared.Mocks.Taxi.mockList
import javax.inject.Inject

interface TaxiRoomRepositoryProtocol {
    suspend fun fetchRooms(): List<TaxiRoom>
    suspend fun fetchMyRooms(): Pair<List<TaxiRoom>, List<TaxiRoom>>
    suspend fun fetchLocations(): List<TaxiLocation>
    suspend fun createRoom(with: TaxiCreateRoom): TaxiRoom
    suspend fun joinRoom(id: String): TaxiRoom
    suspend fun leaveRoom(id: String): TaxiRoom
    suspend fun getRoom(id: String): TaxiRoom
    suspend fun getPublicRoom(id: String): TaxiRoom
    suspend fun commitSettlement(id: String): TaxiRoom
    suspend fun commitPayment(id: String): TaxiRoom
    suspend fun toggleCarrier(id: String, hasCarrier: Boolean): TaxiRoom
    suspend fun updateArrival(id: String, isArrived: Boolean): TaxiRoom
}

class FakeTaxiRoomRepository : TaxiRoomRepositoryProtocol {
    override suspend fun fetchRooms(): List<TaxiRoom> = listOf(TaxiRoom.mock())
    override suspend fun fetchMyRooms(): Pair<List<TaxiRoom>, List<TaxiRoom>> = Pair(emptyList(), emptyList())
    override suspend fun fetchLocations(): List<TaxiLocation> = TaxiLocation.mockList()
    override suspend fun createRoom(with: TaxiCreateRoom): TaxiRoom = TaxiRoom.mock()
    override suspend fun joinRoom(id: String): TaxiRoom = TaxiRoom.mock()
    override suspend fun leaveRoom(id: String): TaxiRoom = TaxiRoom.mock()
    override suspend fun getRoom(id: String): TaxiRoom = TaxiRoom.mock()
    override suspend fun getPublicRoom(id: String): TaxiRoom = TaxiRoom.mock()
    override suspend fun commitSettlement(id: String): TaxiRoom = TaxiRoom.mock()
    override suspend fun commitPayment(id: String): TaxiRoom = TaxiRoom.mock()
    override suspend fun toggleCarrier(id: String, hasCarrier: Boolean): TaxiRoom = TaxiRoom.mock()
    override suspend fun updateArrival(id: String, isArrived: Boolean): TaxiRoom = TaxiRoom.mock()
}


class TaxiRoomRepository @Inject constructor(
    private val taxiRoomApi: TaxiRoomApi,
    private val gson: Gson = Gson()
) : TaxiRoomRepositoryProtocol {

    override suspend fun fetchRooms(): List<TaxiRoom> = safeApiCall(gson) {
        taxiRoomApi.fetchRooms()
    }.map { it.toModel() }

    override suspend fun fetchMyRooms(): Pair<List<TaxiRoom>, List<TaxiRoom>> = safeApiCall(gson) {
        val response = taxiRoomApi.fetchMyRooms()
        Pair(
            response.onGoing.map { it.toModel() },
            response.done.map { it.toModel() }
        )
    }

    override suspend fun fetchLocations(): List<TaxiLocation> = safeApiCall(gson) {
        taxiRoomApi.fetchLocations().locations
    }.map { it.toModel() }

    override suspend fun createRoom(with: TaxiCreateRoom): TaxiRoom = safeApiCall(gson) {
        val requestDTO = TaxiCreateRoomRequestDTO.fromModel(with)
        taxiRoomApi.createRoom(requestDTO)
    }.toModel()

    override suspend fun joinRoom(id: String): TaxiRoom = safeApiCall(gson) {
        taxiRoomApi.joinRoom(mapOf("roomId" to id))
    }.toModel()

    override suspend fun leaveRoom(id: String): TaxiRoom = safeApiCall(gson) {
        taxiRoomApi.leaveRoom(mapOf("roomId" to id))
    }.toModel()

    override suspend fun getRoom(id: String): TaxiRoom = safeApiCall(gson) {
        taxiRoomApi.getRoom(id)
    }.toModel()

    override suspend fun getPublicRoom(id: String): TaxiRoom = safeApiCall(gson) {
        taxiRoomApi.getPublicRoom(id)
    }.toModel()

    override suspend fun commitSettlement(id: String): TaxiRoom = safeApiCall(gson) {
        taxiRoomApi.commitSettlement(mapOf("roomId" to id))
    }.toModel()

    override suspend fun commitPayment(id: String): TaxiRoom = safeApiCall(gson) {
        taxiRoomApi.commitPayment(mapOf("roomId" to id))
    }.toModel()

    override suspend fun toggleCarrier(id: String, hasCarrier: Boolean): TaxiRoom = safeApiCall(gson) {
        taxiRoomApi.toggleCarrier(
            mapOf(
                "roomId" to id,
                "hasCarrier" to hasCarrier
            )
        )
    }.toModel()

    override suspend fun updateArrival(id: String, isArrived: Boolean): TaxiRoom = safeApiCall(gson) {
        taxiRoomApi.updateArrival(
            mapOf(
                "roomId" to id,
                "isArrived" to isArrived
            )
        )
    }.toModel()
}
