package org.sparcs.soap.app.networking.responseDTO.ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.ara.AraPostAuthorProfile
import java.net.URL

data class AraPostAuthorProfileDTO(
    @SerializedName("user")
    val id: Any,

    @SerializedName("picture")
    val profilePictureURL: String?,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("is_official")
    val isOfficial: Boolean?,

    @SerializedName("is_school_admin")
    val isSchoolAdmin: Boolean?
) {
    fun toModel(): AraPostAuthorProfile =
        AraPostAuthorProfile(
            id = id.toString(),
            profilePictureURL = profilePictureURL?.let { try { URL(it) } catch (e: Exception) { null } },
            nickname = nickname,
            isOfficial = isOfficial,
            isSchoolAdmin = isSchoolAdmin
        )
}
