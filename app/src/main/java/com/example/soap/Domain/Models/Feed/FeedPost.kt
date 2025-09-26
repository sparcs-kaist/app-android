package com.example.soap.Domain.Models.Feed

import com.example.soap.Domain.Enums.FeedVoteType
import java.net.URL
import java.util.Date

data class FeedPost(
    val id: String,
    val content: String,
    val isAnonymous: Boolean,
    val authorName: String,
    val nickname: String?,
    val profileImageURL: URL?,
    val createdAt: Date,
    val commentCount: Int,
    val upVotes: Int,
    val downVotes: Int,
    val myVote: FeedVoteType?,
    val isAuthor: Boolean,
    val images: List<FeedImage>
) {
    companion object {}
}