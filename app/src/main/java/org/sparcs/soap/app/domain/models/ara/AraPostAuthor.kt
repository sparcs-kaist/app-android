package org.sparcs.soap.app.domain.models.ara

data class AraPostAuthor(
    val id: String,
    val username: String,
    val profile: AraPostAuthorProfile,
    val isBlocked: Boolean?
){
    companion object
}