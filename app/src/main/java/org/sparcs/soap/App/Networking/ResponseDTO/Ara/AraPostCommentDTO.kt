package org.sparcs.soap.App.Networking.ResponseDTO.Ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.Ara.AraPostComment
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Date

data class AraPostCommentDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("is_hidden")
    val isHidden: Boolean?,

    @SerializedName("why_hidden")
    val hiddenReason: List<String>?,

    @SerializedName("can_override_hidden")
    val overrideHidden: Boolean?,

    @SerializedName("my_vote")
    val myVote: Boolean?,

    @SerializedName("is_mine")
    val isMine: Boolean?,

    @SerializedName("content")
    val content: String?,

    @SerializedName("created_by")
    val author: AraPostAuthorDTO,

    @SerializedName("comments")
    val comments: List<AraPostCommentDTO>?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("positive_vote_count")
    val upVotes: Int,

    @SerializedName("negative_vote_count")
    val downVotes: Int,

    @SerializedName("parent_article")
    val parentPost: Int?,

    @SerializedName("parent_comment")
    val parentComment: Int?
) {
    fun toModel(): AraPostComment = AraPostComment(
        id = id,
        isHidden = isHidden,
        hiddenReason = hiddenReason,
        overrideHidden = overrideHidden,
        myVote = myVote,
        isMine = isMine,
        content = content,
        author = author.toModel(),
        comments = comments?.map { it.toModel() }?.toMutableList() ?: mutableListOf(),
        createdAt = try {
            Date.from(Instant.parse(createdAt))
        } catch (e: DateTimeParseException) {
            Date()
        },
        upVotes = upVotes,
        downVotes = downVotes,
        parentPost = parentPost,
        parentComment = parentComment
    )
}
