package com.example.soap.Networking.ResponseDTO.Taxi

import com.example.soap.Domain.Models.Taxi.TaxiParticipant
import com.example.soap.Shared.Extensions.toDate
import com.google.gson.annotations.SerializedName
import java.net.URL
import java.util.Date

data class TaxiParticipantDTO(
    @SerializedName("_id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("profileImageUrl")
    val profileImageURL: String,

    @SerializedName("withdraw")
    val withdraw: Boolean,

    @SerializedName("readAt")
    val readAt: String
) {
    fun toModel(): TaxiParticipant {
        return TaxiParticipant(
            id = id,
            name = name,
            nickname = nickname,
            profileImageURL = URL(profileImageURL),
            withdraw = withdraw,
            readAt = readAt.toDate() ?: Date()
        )
    }
}
