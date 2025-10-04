package com.example.soap.Domain.Models.Feed

import com.example.soap.Domain.Enums.FeedVoteType
import java.util.Date

data class FeedComment(
    val id: String,
    val postID: String,
    val parentCommentID: String?,
    val content: String,
    val isDeleted: Boolean,
    val isAnonymous: Boolean,
    val authorName: String,
    val isAuthor: Boolean,
    val isMyComment: Boolean,
    val profileImageURL: String?,
    val createdAt: Date,
    val upVotes: Int,
    val downVotes: Int,
    val myVote: FeedVoteType?,
    val image: FeedImage?,
    val replyCount: Int,
    val replies: List<FeedComment>
) {
    companion object {}
}