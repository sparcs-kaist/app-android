package com.sparcs.soap.Networking.ResponseDTO.Feed

import com.sparcs.soap.Domain.Enums.FeedVoteType
import com.sparcs.soap.Domain.Models.Feed.FeedComment
import com.sparcs.soap.Shared.Extensions.toDate
import com.google.gson.annotations.SerializedName
import java.util.Date

data class FeedCommentDTO (
    @SerializedName("id")
    val id: String,

    @SerializedName("post_id")
    val postID: String,

    @SerializedName("parent_comment_id")
    val parentCommentID: String?,

    @SerializedName("content")
    val content: String,

    @SerializedName("is_deleted")
    val isDeleted: Boolean,

    @SerializedName("is_anonymous")
    val isAnonymous: Boolean,

    @SerializedName("author_name")
    val authorName: String,

    @SerializedName("is_author")
    val isAuthor: Boolean,

    @SerializedName("is_my_comment")
    val isMyComment: Boolean,

    @SerializedName("profile_image_url")
    val profileImageURL: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("upvotes")
    val upVotes: Int,

    @SerializedName("downvotes")
    val downVotes: Int,

    @SerializedName("my_vote")
    val myVote: String?,

    @SerializedName("image")
    val image: FeedImageDTO?,

    @SerializedName("reply_count")
    val replyCount: Int,

    @SerializedName("replies")
    val replies: List<FeedCommentDTO>
) {
    fun toModel(): FeedComment {
        return FeedComment(
            id = id,
            postID = postID,
            parentCommentID = parentCommentID,
            content = content,
            isDeleted = isDeleted,
            isAnonymous = isAnonymous,
            authorName = authorName,
            isAuthor = isAuthor,
            isMyComment = isMyComment,
            profileImageURL= profileImageURL,
            createdAt = createdAt.toDate() ?: Date(),
            upVotes = upVotes,
            downVotes = downVotes,
            myVote = myVote?.let { type -> FeedVoteType.entries.find { it.name == type }},
            image = image?.toModel(),
            replyCount = replyCount,
            replies = replies.map { it.toModel() }
        )
    }
}