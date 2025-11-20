package com.sparcs.soap.Domain.Enums

sealed class PostListType {
    data object All : PostListType()
    data class Board(val boardID: Int) : PostListType()
    data class User(val userID: Int) : PostListType()
}