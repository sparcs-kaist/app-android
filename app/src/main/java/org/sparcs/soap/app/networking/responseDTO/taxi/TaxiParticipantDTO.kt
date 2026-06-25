package org.sparcs.soap.app.networking.responseDTO.taxi

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.shared.extensions.toDate
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

    @SerializedName("badge")
    val badge: Boolean?,

    @SerializedName("isSettlement")
    val isSettlement: String?,

    @SerializedName("readAt")
    val readAt: String,

    @SerializedName("hasCarrier")
    val hasCarrier: Boolean?,

    @SerializedName("isArrived")
    val isArrived: Boolean?
) {
    fun toModel(): TaxiParticipant {
        return TaxiParticipant(
            id = id,
            name = name,
            nickname = nickname,
            profileImageURL = URL(profileImageURL),
            withdraw = withdraw,
            badge = badge ?: false,//treat as false when null
            isSettlement = isSettlement?.let { TaxiParticipant.SettlementType.from(it) },
            readAt = readAt.toDate() ?: Date(),
            hasCarrier = hasCarrier ?: false,
            isArrived = isArrived ?: false
        )
    }
}
