package org.sparcs.App.Networking.ResponseDTO.Ara

import com.google.gson.annotations.SerializedName
import org.sparcs.App.Domain.Models.Ara.AraUser
import org.sparcs.App.Shared.Extensions.toDate

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