package org.sparcs.soap.App.Networking.ResponseDTO.Auth

import com.google.gson.annotations.SerializedName

data class SignInResponseDTO(
    @SerializedName("accessToken")
    val accessToken : String,

    @SerializedName("refreshToken")
    val refreshToken : String,

    @SerializedName("ssoInfo")
    val ssoInfo : String
)