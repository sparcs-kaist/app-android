package org.sparcs.Networking.ResponseDTO.Ara

import com.google.gson.annotations.SerializedName
import org.sparcs.Domain.Models.Ara.AraUser
import org.sparcs.Shared.Extensions.toDate

data class AraUserDTO(
    @SerializedName("user")
    val id: Int,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("nickname_updated_at")
    val nicknameUpdatedAt: String,

    @SerializedName("see_sexual")
    val allowNSFW: Boolean,

    @SerializedName("see_social")
    val allowPolitical: Boolean
){
    fun toModel(): AraUser {
        return AraUser(
            id = id,
            nickname = nickname,
            nicknameUpdatedAt = nicknameUpdatedAt.toDate(),
            allowNSFW = allowNSFW,
            allowPolitical = allowPolitical
        )
    }
}