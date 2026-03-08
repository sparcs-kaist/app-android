package org.sparcs.soap.App.Domain.Models.Ara

data class AraPostAuthor(
    val id: String,
    val username: String,
    val profile: AraPostAuthorProfile,
    val isBlocked: Boolean?
){
    companion object
}