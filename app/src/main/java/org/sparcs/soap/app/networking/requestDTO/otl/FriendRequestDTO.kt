package org.sparcs.soap.app.networking.requestDTO.otl

data class AddFriendRequestDTO(
    val code: String,
)

data class SetFriendFavoriteRequestDTO(
    val isFavorite: Boolean,
)
