package org.sparcs.soap.app.networking.responseDTO.ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.ara.AraBlockedUser

data class AraBlockPageDTO(
    @SerializedName("num_pages")
    val numPages: Int,

    @SerializedName("current")
    val current: Int,

    @SerializedName("results")
    val results: List<AraBlockDTO>,
)

data class AraBlockDTO(
    @SerializedName("user")
    val user: AraPostAuthorDTO,
) {
    fun toModel(): AraBlockedUser? {
        val id = user.id.toString().toDoubleOrNull()?.toInt() ?: return null
        val author = user.toModel()
        return AraBlockedUser(id = id, nickname = author.profile.nickname, profilePictureURL = author.profile.profilePictureURL)
    }
}
