package org.sparcs.soap.app.domain.models.ara

data class AraPostAuthor(
    val id: String,
    val username: String,
    val profile: AraPostAuthorProfile,
    val isBlocked: Boolean?
){
    val blockableUserId: Int?
        get() = if (isBlocked == null) null else id.toDoubleOrNull()?.takeIf { it == Math.floor(it) }?.toInt()

    companion object
}