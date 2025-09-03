package com.example.soap.Domain.Models.Ara

import java.util.Date

data class AraPostComment (
    val id: Int,
    val isHidden: Boolean?,
    val hiddenReason: List<String>?,
    val overrideHidden: Boolean?,
    val myVote: Boolean?,
    val isMine: Boolean?,
    val content: String?,
    val author: AraPostAuthor,
    val comments: List<AraPostComment>?,
    val createdAt: Date,
    val upVotes: Int,
    val downVotes: Int,
    val parentPost: Int?,
    val parentComment: Int?
){
    companion object{}
}