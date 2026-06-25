package org.sparcs.soap.app.networking.responseDTO.auth

import com.google.gson.annotations.SerializedName

data class TokenResponseDTO(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String
)
