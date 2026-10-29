package org.sparcs.soap.app.domain.enums.ara

sealed class PostListType {
    data object All : PostListType()
    data class Board(val boardID: Int, val topicID: Int? = null) : PostListType()
    data class User(val userID: Int) : PostListType()
    data object Trending : PostListType()
    data class BoardSlug(val slug: String) : PostListType()
    data class Latest(val boardIDs: List<Int>) : PostListType()
}