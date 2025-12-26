package org.sparcs.Domain.Models.Feed


data class FeedUser(
    val id: String,
    val nickname: String,
    val profileImageURL: String?,
    val karma: Int
)