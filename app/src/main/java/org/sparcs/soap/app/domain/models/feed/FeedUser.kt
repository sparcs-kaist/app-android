package org.sparcs.soap.app.domain.models.feed


data class FeedUser(
    val id: String,
    val nickname: String,
    val profileImageURL: String?,
    val karma: Int
)