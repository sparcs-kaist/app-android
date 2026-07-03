package org.sparcs.soap.app.domain.enums.ara

sealed class PostListType {
    data object All : PostListType()
    data class Board(val boardID: Int) : PostListType()
    data class User(val userID: Int) : PostListType()
}