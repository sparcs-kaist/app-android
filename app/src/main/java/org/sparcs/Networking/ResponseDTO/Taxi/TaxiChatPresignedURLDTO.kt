package org.sparcs.Networking.ResponseDTO.Taxi

import com.google.gson.annotations.SerializedName

data class TaxiChatPresignedURLDTO(
    @SerializedName("id")
    val id: String,

    @SerializedName("url")
    val url: String,
)