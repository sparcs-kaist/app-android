package com.example.soap.Domain.Models.Ara

data class AraPostAuthor(
    val id: String,
    val username: String,
    val profile: AraPostAuthorProfile,
    val isBlocked: Boolean?
)