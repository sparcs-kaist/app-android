package org.sparcs.soap.App.Networking.ResponseDTO.Taxi

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Enums.Taxi.EmojiIdentifier
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Shared.Extensions.toDate
import java.util.Date

data class TaxiRoomDTO(
    @SerializedName("_id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("from")
    val from: TaxiLocationDTO,

    @SerializedName("to")
    val to: TaxiLocationDTO,

    @SerializedName("time")
    val time: String,

    @SerializedName("part")
    val participants: List<TaxiParticipantDTO>,

    @SerializedName("madeat")
    val madeAt: String,

    @SerializedName("maxPartLength")
    val maxParticipants: Int,

    @SerializedName("settlementTotal")
    val settlementTotal: Int?,

    @SerializedName("emojiIdentifier")
    val emojiIdentifier: String?,

    @SerializedName("isDeparted")
    val isDeparted: Boolean,

    @SerializedName("isOver")
    val isOver: Boolean?
) {
    fun toModel(): TaxiRoom {
        return TaxiRoom(
            id = id,
            title = name,
            source = from.toModel(),
            destination = to.toModel(),
            departAt = time.toDate() ?: Date(),
            participants = participants.map { it.toModel() },
            madeAt = madeAt.toDate() ?: Date(),
            capacity = maxParticipants,
            settlementTotal = settlementTotal,
            emojiIdentifier = EmojiIdentifier.fromRawValue(emojiIdentifier),
            isDeparted = isDeparted,
            isOver = isOver
        )
    }
}
