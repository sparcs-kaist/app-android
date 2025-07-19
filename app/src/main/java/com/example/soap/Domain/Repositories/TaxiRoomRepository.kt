package com.example.soap.Domain.Repositories

import com.example.soap.Domain.Models.Taxi.TaxiCreateRoom
import com.example.soap.Domain.Models.Taxi.TaxiLocation
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Networking.RequestDTO.TaxiCreateRoomRequestDTO
import com.example.soap.Networking.ResponseDTO.ApiErrorResponse
import com.example.soap.Networking.ResponseDTO.DomainException
import com.example.soap.Networking.ResponseDTO.Taxi.TaxiLocationResponseDTO
import com.example.soap.Networking.ResponseDTO.Taxi.TaxiMyRoomsResponseDTO
import com.example.soap.Networking.ResponseDTO.Taxi.TaxiRoomDTO
import com.example.soap.Shared.Mocks.mock
import com.example.soap.Shared.Mocks.mockList
import com.google.gson.Gson
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.IOException
import javax.inject.Inject

interface TaxiRoomService {
    @GET("fetchRooms")
    suspend fun fetchRooms(): Response<List<TaxiRoomDTO>>

    @GET("fetchMyRooms")
    suspend fun fetchMyRooms(): Response<TaxiMyRoomsResponseDTO>

    @GET("fetchLocations")
    suspend fun fetchLocations(): Response<TaxiLocationResponseDTO>

    @POST("createRoom")
    suspend fun createRoom(@Body request: TaxiCreateRoomRequestDTO): Response<TaxiRoomDTO>

    @POST("joinRoom/{id}")
    suspend fun joinRoom(@Path("id") id: String): Response<TaxiRoomDTO>
}

class FakeTaxiRoomRepository : TaxiRoomRepositoryProtocol {
    override suspend fun fetchRooms(): List<TaxiRoom> = listOf(TaxiRoom.mock())
    override suspend fun fetchMyRooms(): Pair<List<TaxiRoom>, List<TaxiRoom>> = Pair(emptyList(), emptyList())
    override suspend fun fetchLocations(): List<TaxiLocation> = TaxiLocation.mockList()
    override suspend fun createRoom(with: TaxiCreateRoom): TaxiRoom = TaxiRoom.mock()
    override suspend fun joinRoom(id: String): TaxiRoom = TaxiRoom.mock()
}


class TaxiRoomRepository @Inject constructor(
    private val taxiRoomService: TaxiRoomService,
    private val gson: Gson = Gson()
) : TaxiRoomRepositoryProtocol {

    private suspend fun <T, R> processResponse(
        apiCall: suspend () -> Response<T>,
        mapper: (T) -> R
    ): R {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let {
                    return mapper(it)
                } ?: throw DomainException("API response body is null", httpStatusCode = response.code())
            } else {
                val errorBodyString = response.errorBody()?.string()
                if (!errorBodyString.isNullOrBlank()) {
                    try {
                        val apiError = gson.fromJson(errorBodyString, ApiErrorResponse::class.java)
                        throw apiError.toDomainError().copy(httpStatusCode = response.code())
                    } catch (e: Exception) {
                        throw DomainException(
                            message = "Error: ${response.code()} ${response.message()}. Failed to parse error body: $errorBodyString",
                            httpStatusCode = response.code()
                        )
                    }
                } else {
                    throw DomainException(
                        message = "API call failed with code ${response.code()}: ${response.message()}",
                        httpStatusCode = response.code()
                    )
                }
            }
        } catch (e: IOException) {
            throw DomainException("Network error: ${e.message ?: "Unknown network error"}")
        } catch (e: DomainException) {
            throw e
        } catch (e: Exception) {
            throw DomainException("An unexpected error occurred: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun fetchRooms(): List<TaxiRoom> {
        return processResponse(
            apiCall = { taxiRoomService.fetchRooms() },
            mapper = { DTOList -> DTOList.map { it.toModel() } }
        )
    }
    override suspend fun fetchMyRooms(): Pair<List<TaxiRoom>, List<TaxiRoom>> {
        return processResponse(
            apiCall = { taxiRoomService.fetchMyRooms() },
            mapper = { responseDto ->
                val onGoingRooms = responseDto.onGoing.map { it.toModel() }
                val doneRooms = responseDto.done.map { it.toModel() }
                Pair(onGoingRooms, doneRooms)
            }
        )
    }

    override suspend fun fetchLocations(): List<TaxiLocation> {
        return processResponse(
            apiCall = { taxiRoomService.fetchLocations() },
            mapper = { responseDto -> responseDto.locations.map { it.toModel() } }
        )
    }

    override suspend fun createRoom(with: TaxiCreateRoom): TaxiRoom {
        val requestDTO = TaxiCreateRoomRequestDTO.fromModel(with)
        return processResponse(
            apiCall = { taxiRoomService.createRoom(requestDTO) },
            mapper = { dto ->
                dto.toModel()
            }
        )
    }

    override suspend fun joinRoom(id: String): TaxiRoom {
        return processResponse(
            apiCall = { taxiRoomService.joinRoom(id) },
            mapper = { dto ->
                dto.toModel()
            }
        )
    }
}
