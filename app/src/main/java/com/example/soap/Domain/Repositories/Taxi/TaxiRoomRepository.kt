package com.example.soap.Domain.Repositories.Taxi

import com.example.soap.Domain.Models.Taxi.TaxiCreateRoom
import com.example.soap.Domain.Models.Taxi.TaxiLocation
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Networking.RequestDTO.TaxiCreateRoomRequestDTO
import com.example.soap.Networking.ResponseDTO.ApiErrorResponse
import com.example.soap.Networking.RetrofitAPI.Taxi.TaxiRoomApi
import com.example.soap.Shared.Mocks.mock
import com.example.soap.Shared.Mocks.mockList
import com.google.gson.Gson
import retrofit2.HttpException
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

    private fun handleApiError(gson: Gson, exception: Exception): Nothing {
        if (exception is HttpException) {
            val errorBody = exception.response()?.errorBody()?.string()
            if (errorBody != null) {
                val parsedError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                throw parsedError.toDomainError()
            } else {
                throw exception
            }
        } else {
            throw exception
        }
    }

}
