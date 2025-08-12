package com.example.soap.Domain.Repositories

import com.example.soap.Domain.Models.Taxi.TaxiCreateRoom
import com.example.soap.Domain.Models.Taxi.TaxiLocation
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Networking.RequestDTO.TaxiCreateRoomRequestDTO
import com.example.soap.Networking.ResponseDTO.ApiErrorResponse
import com.example.soap.Networking.RetrofitAPI.TaxiRoomService
import com.example.soap.Shared.Mocks.mock
import com.example.soap.Shared.Mocks.mockList
import com.google.gson.Gson
import retrofit2.HttpException
import javax.inject.Inject

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
    private val taxiRoomService: TaxiRoomService,
    private val gson: Gson = Gson()
) : TaxiRoomRepositoryProtocol {

    override suspend fun fetchRooms(): List<TaxiRoom> {
        return try {
            taxiRoomService.fetchRooms()
                .map { it.toModel() }
        } catch (e: Exception) {
            throw handleApiError(gson, e)
        }
    }

    override suspend fun fetchMyRooms(): Pair<List<TaxiRoom>, List<TaxiRoom>> {
        return try {
            val response = taxiRoomService.fetchMyRooms()
            val onGoing = response.onGoing.map { it.toModel() }
            val done = response.done.map { it.toModel() }
            Pair(onGoing, done)
        } catch (e: Exception) {
            throw handleApiError(gson, e)
        }
    }

    override suspend fun fetchLocations(): List<TaxiLocation> {
        return try {
            taxiRoomService.fetchLocations()
                .locations
                .map { it.toModel() }
        } catch (e: Exception) {
            throw handleApiError(gson, e)
        }
    }

    override suspend fun createRoom(with: TaxiCreateRoom): TaxiRoom {
        return try {
            val requestDTO = TaxiCreateRoomRequestDTO.fromModel(with)
            taxiRoomService.createRoom(requestDTO).toModel()
        } catch (e: Exception) {
            throw handleApiError(gson, e)
        }
    }

    override suspend fun joinRoom(id: String): TaxiRoom {
        return try {
            taxiRoomService.joinRoom(mapOf("roomId" to id)).toModel()
        } catch (e: Exception) {
            throw handleApiError(gson, e)
        }
    }

    override suspend fun leaveRoom(id: String): TaxiRoom {
        return try {
            taxiRoomService.leaveRoom(mapOf("roomId" to id)).toModel()
        } catch (e: Exception) {
            throw handleApiError(gson, e)
        }
    }

    override suspend fun getRoom(id: String): TaxiRoom {
        return try {
            taxiRoomService.getRoom(id).toModel()
        } catch (e: Exception) {
            throw handleApiError(gson, e)
        }
    }

    override suspend fun commitSettlement(id: String): TaxiRoom {
        return try {
            taxiRoomService.commitSettlement(mapOf("roomId" to id)).toModel()
        } catch (e: Exception) {
            throw handleApiError(gson, e)
        }
    }

    override suspend fun commitPayment(id: String): TaxiRoom {
        return try {
            taxiRoomService.commitPayment(mapOf("roomId" to id)).toModel()
        } catch (e: Exception) {
            throw handleApiError(gson, e)
        }
    }

    fun <T> handleApiError(gson: Gson, exception: Exception): T {
        return try {
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
        } catch (e: Exception) {
            throw e
        }
    }
}
