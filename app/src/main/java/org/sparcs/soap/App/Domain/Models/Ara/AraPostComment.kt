package org.sparcs.soap.App.Domain.Models.Ara

import java.util.Date

data class AraPostComment (
    val id: Int,
    val isHidden: Boolean?,
    val hiddenReason: List<String>?,
    val overrideHidden: Boolean?,
    var myVote: Boolean?,
    var isMine: Boolean?,
    var content: String?,
    val author: AraPostAuthor,
    val comments:  MutableList<AraPostComment>,
    val createdAt: Date,
    var upVotes: Int,
    var downVotes: Int,
    val parentPost: Int?,
    val parentComment: Int?
){
    companion object{}
}