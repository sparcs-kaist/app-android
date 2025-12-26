package org.sparcs.Domain.Repositories.Taxi

import org.sparcs.Domain.Models.Taxi.TaxiCreateRoom
import org.sparcs.Domain.Models.Taxi.TaxiLocation
import org.sparcs.Domain.Models.Taxi.TaxiRoom
import org.sparcs.Networking.RequestDTO.Taxi.TaxiCreateRoomRequestDTO
import org.sparcs.Networking.ResponseDTO.handleApiError
import org.sparcs.Networking.RetrofitAPI.Taxi.TaxiRoomApi
import org.sparcs.Shared.Mocks.mock
import org.sparcs.Shared.Mocks.mockList
import com.google.gson.Gson
import javax.inject.Inject

interface TaxiRoomRepositoryProtocol {
    suspend fun fetchRooms(): List<TaxiRoom>
    suspend fun fetchMyRooms(): Pair<List<TaxiRoom>, List<TaxiRoom>>
    suspend fun fetchLocations(): List<TaxiLocation>
    suspend fun createRoom(with: TaxiCreateRoom): TaxiRoom
    suspend fun joinRoom(id: String): TaxiRoom
    suspend fun leaveRoom(id: String): TaxiRoom
    suspend fun getRoom(id: String): TaxiRoom
    suspend fun commitSettlement(id: String): TaxiRoom
    suspend fun commitPayment(id: String): TaxiRoom
}

class FakeTaxiRoomRepository : TaxiRoomRepositoryProtocol {
    override suspend fun fetchRooms(): List<TaxiRoom> = listOf(TaxiRoom.mock())
    override suspend fun fetchMyRooms(): Pair<List<TaxiRoom>, List<TaxiRoom>> = Pair(emptyList(), emptyList())
    override suspend fun fetchLocations(): List<TaxiLocation> = TaxiLocation.mockList()
    override suspend fun createRoom(with: TaxiCreateRoom): TaxiRoom = TaxiRoom.mock()
    override suspend fun joinRoom(id: String): TaxiRoom = TaxiRoom.mock()
    override suspend fun leaveRoom(id: String): TaxiRoom = TaxiRoom.mock()
    override suspend fun getRoom(id: String): TaxiRoom = TaxiRoom.mock()
    override suspend fun commitSettlement(id: String): TaxiRoom = TaxiRoom.mock()
    override suspend fun commitPayment(id: String): TaxiRoom = TaxiRoom.mock()
}


class TaxiRoomRepository @Inject constructor(
    private val taxiRoomApi: TaxiRoomApi,
    private val gson: Gson = Gson()
) : TaxiRoomRepositoryProtocol {

    override suspend fun fetchRooms(): List<TaxiRoom> {
        return try {
            taxiRoomApi.fetchRooms()
                .map { it.toModel() }
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun fetchMyRooms(): Pair<List<TaxiRoom>, List<TaxiRoom>> {
        return try {
            val response = taxiRoomApi.fetchMyRooms()
            val onGoing = response.onGoing.map { it.toModel() }
            val done = response.done.map { it.toModel() }
            Pair(onGoing, done)
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun fetchLocations(): List<TaxiLocation> {
        return try {
            taxiRoomApi.fetchLocations()
                .locations
                .map { it.toModel() }
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun createRoom(with: TaxiCreateRoom): TaxiRoom {
        return try {
            val requestDTO = TaxiCreateRoomRequestDTO.fromModel(with)
            taxiRoomApi.createRoom(requestDTO).toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun joinRoom(id: String): TaxiRoom {
        return try {
            taxiRoomApi.joinRoom(mapOf("roomId" to id)).toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun leaveRoom(id: String): TaxiRoom {
        return try {
            taxiRoomApi.leaveRoom(mapOf("roomId" to id)).toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun getRoom(id: String): TaxiRoom {
        return try {
            taxiRoomApi.getRoom(id).toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun commitSettlement(id: String): TaxiRoom {
        return try {
            taxiRoomApi.commitSettlement(mapOf("roomId" to id)).toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun commitPayment(id: String): TaxiRoom {
        return try {
            taxiRoomApi.commitPayment(mapOf("roomId" to id)).toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }
}
