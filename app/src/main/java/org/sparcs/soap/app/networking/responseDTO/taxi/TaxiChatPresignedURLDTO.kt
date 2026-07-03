package org.sparcs.soap.app.networking.responseDTO.taxi

import com.google.gson.annotations.SerializedName

data class TaxiChatPresignedURLDTO(
    @SerializedName("id")
    val id: String,

    @SerializedName("url")
    val url: String,
)