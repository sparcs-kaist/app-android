package org.sparcs.soap.app.networking.responseDTO.taxi

import com.google.gson.annotations.SerializedName

data class TaxiChatResponseDTO(
    @SerializedName("result")
    val result: Boolean
)