package org.sparcs.soap.App.Networking.RetrofitAPI.Taxi

import org.sparcs.soap.App.Networking.RequestDTO.Taxi.TaxiCreateRoomRequestDTO
import org.sparcs.soap.App.Networking.ResponseDTO.Taxi.TaxiLocationResponseDTO
import org.sparcs.soap.App.Networking.ResponseDTO.Taxi.TaxiMyRoomsResponseDTO
import org.sparcs.soap.App.Networking.ResponseDTO.Taxi.TaxiRoomDTO
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

    @GET("rooms/publicInfo")
    suspend fun getPublicRoom(
        @Query("id") roomId: String,
    ): TaxiRoomDTO

    @POST("rooms/commitSettlement")
    suspend fun commitSettlement(@Body body: Map<String, String>): TaxiRoomDTO

    @POST("rooms/commitPayment")
    suspend fun commitPayment(@Body body: Map<String, String>): TaxiRoomDTO

    @POST("rooms/carrier/toggle")
    suspend fun toggleCarrier(@Body body: Map<String, @JvmSuppressWildcards Any>): TaxiRoomDTO

    @POST("rooms/updateArrival")
    suspend fun updateArrival(@Body body: Map<String, @JvmSuppressWildcards Any>): TaxiRoomDTO
}
