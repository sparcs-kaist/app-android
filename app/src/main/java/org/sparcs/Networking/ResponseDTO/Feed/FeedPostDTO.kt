package org.sparcs.Networking.ResponseDTO.Feed

import com.google.gson.annotations.SerializedName
import org.sparcs.Domain.Enums.Feed.FeedVoteType
import org.sparcs.Domain.Models.Feed.FeedPost
import org.sparcs.Shared.Extensions.toDate
import java.util.Date

data class FeedPostDTO (
    @SerializedName("id")
    val id: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("is_anonymous")
    val isAnonymous: Boolean,

    @SerializedName("is_kaist_ip")
    val isKaistIP: Boolean,

    @SerializedName("author_name")
    val authorName: String,

    @SerializedName("nickname")
    val nickname: String?,

    @SerializedName("profile_image_url")
    val profileImageURL: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("comment_count")
    val commentCount: Int,

    @SerializedName("upvotes")
    val upVotes: Int,

    @SerializedName("downvotes")
    val downVotes: Int,

    @SerializedName("my_vote")
    val myVote: String?,

    @SerializedName("is_author")
    val isAuthor: Boolean,

    @SerializedName("images")
    val images: List<FeedImageDTO>
) {
    fun toModel(): FeedPost {
        return FeedPost(
            id = id,
            content = content,
            isAnonymous = isAnonymous,
            isKaistIP = isKaistIP,
            authorName = authorName,
            nickname = nickname,
            profileImageURL = profileImageURL,
            createdAt = createdAt.toDate() ?: Date(),
            commentCount = commentCount,
            upVotes = upVotes,
            downVotes = downVotes,
            myVote = myVote?.let { type -> FeedVoteType.entries.find { it.name == type } },
            isAuthor = isAuthor,
            images = images.map { it.toModel() }
        )
    }
}