package org.sparcs.soap.App.Networking.ResponseDTO.Taxi

import com.google.gson.annotations.SerializedName

data class TaxiMyRoomsResponseDTO(
    @SerializedName("ongoing")
    val onGoing: List<TaxiRoomDTO>,

    @SerializedName("done")
    val done: List<TaxiRoomDTO>
)
