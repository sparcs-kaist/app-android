package com.sparcs.soap.Domain.Models.Feed

import com.sparcs.soap.Domain.Enums.Feed.FeedVoteType
import java.util.Date

data class FeedPost(
    val id: String,
    val content: String,
    val isAnonymous: Boolean,
    val authorName: String,
    val nickname: String?,
    val profileImageURL: String?,
    val createdAt: Date,
    var commentCount: Int,
    val upVotes: Int,
    val downVotes: Int,
    val myVote: FeedVoteType?,
    val isAuthor: Boolean,
    val images: List<FeedImage>
) {
    companion object {}
}