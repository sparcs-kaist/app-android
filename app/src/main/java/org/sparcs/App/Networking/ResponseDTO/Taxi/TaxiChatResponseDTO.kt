package org.sparcs.App.Networking.ResponseDTO.Taxi

import com.google.gson.annotations.SerializedName

data class TaxiChatResponseDTO(
    @SerializedName("result")
    val result: Boolean
)