package com.sparcs.soap.Networking.ResponseDTO.Ara

import com.google.gson.annotations.SerializedName

data class AraSignInResponseDTO(
    @SerializedName("uid")
    val uid: String,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("user_id")
    val userID: Int,
)