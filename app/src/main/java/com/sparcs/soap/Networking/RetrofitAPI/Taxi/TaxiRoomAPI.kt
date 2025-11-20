package com.sparcs.soap.Networking.RetrofitAPI.Taxi

import com.sparcs.soap.Networking.RequestDTO.Taxi.TaxiCreateRoomRequestDTO
import com.sparcs.soap.Networking.ResponseDTO.Taxi.TaxiLocationResponseDTO
import com.sparcs.soap.Networking.ResponseDTO.Taxi.TaxiMyRoomsResponseDTO
import com.sparcs.soap.Networking.ResponseDTO.Taxi.TaxiRoomDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TaxiRoomApi {

    @GET("rooms/search")
    suspend fun fetchRooms(): List<TaxiRoomDTO>

    @GET("rooms/searchByUser")
    suspend fun fetchMyRooms(): TaxiMyRoomsResponseDTO

    @GET("locations")
    suspend fun fetchLocations(): TaxiLocationResponseDTO

    @POST("rooms/create")
    suspend fun createRoom(@Body request: TaxiCreateRoomRequestDTO): TaxiRoomDTO

    @POST("rooms/join")
    suspend fun joinRoom(@Body body: Map<String, String>): TaxiRoomDTO

    @POST("rooms/abort")
    suspend fun leaveRoom(@Body body: Map<String, String>): TaxiRoomDTO

    @GET("rooms/info")
    suspend fun getRoom(@Query("id") roomId: String): TaxiRoomDTO

    @POST("rooms/commitSettlement")
    suspend fun commitSettlement(@Body body: Map<String, String>): TaxiRoomDTO

    @POST("rooms/commitPayment")
    suspend fun commitPayment(@Body body: Map<String, String>): TaxiRoomDTO
}
