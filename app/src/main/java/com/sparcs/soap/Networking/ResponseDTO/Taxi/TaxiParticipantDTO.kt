package com.sparcs.soap.Networking.ResponseDTO.Taxi

import com.sparcs.soap.Domain.Models.Taxi.TaxiParticipant
import com.sparcs.soap.Shared.Extensions.toDate
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

    @SerializedName("isSettlement")
    val isSettlement: String?,

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
            isSettlement = isSettlement?.let { TaxiParticipant.SettlementType.from(it) },
            readAt = readAt.toDate() ?: Date()
        )
    }
}
