package org.sparcs.soap.App.Networking.ResponseDTO.Ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Date

data class AraPostDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("is_hidden")
    val isHidden: Boolean,

    @SerializedName("why_hidden")
    val hiddenReason: List<String>,

    @SerializedName("can_override_hidden")
    val overrideHidden: Boolean?,

    @SerializedName("parent_topic")
    val topic: AraBoardTopicDTO?,

    @SerializedName("parent_board")
    val board: AraBoardDTO?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("created_by")
    val author: AraPostAuthorDTO,

    @SerializedName("read_status")
    val readStatus: String?,

    @SerializedName("attachment_type")
    val attachmentType: String?,

    @SerializedName("communication_article_status")
    val communicationArticleStatus: Int?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("is_content_sexual")
    val isNSFW: Boolean,

    @SerializedName("is_content_social")
    val isPolitical: Boolean,

    @SerializedName("hit_count")
    val views: Int,

    @SerializedName("comment_count")
    val commentCount: Int,

    @SerializedName("positive_vote_count")
    val upVotes: Int,

    @SerializedName("negative_vote_count")
    val downVotes: Int,

    @SerializedName("attachments")
    val attachments: List<AraPostAttachmentDTO>?,

    @SerializedName("my_comment_profile")
    val myCommentProfile: AraPostAuthorDTO?,

    @SerializedName("is_mine")
    val isMine: Boolean?,

    @SerializedName("comments")
    val comments: MutableList<AraPostCommentDTO>?,

    @SerializedName("content")
    val content: String?,

    @SerializedName("my_vote")
    val myVote: Boolean?,

    @SerializedName("my_scrap")
    val myScrap: AraScrapDTO?
) {
    fun toModel(): AraPost = AraPost(
        id = id,
        isHidden = isHidden,
        hiddenReason = hiddenReason,
        overrideHidden = overrideHidden,
        topic = topic?.toModel(),
        board = board?.toModel(),
        title = title,
        author = author.toModel(),
        attachmentType = attachmentType?.let { type-> AraPost.AttachmentType.entries.find { it.name == type } } ?: AraPost.AttachmentType.NONE,
        communicationArticleStatus = communicationArticleStatus?.let { statusValue -> AraPost.CommunicationArticleStatus.entries.find { enum -> enum.code == statusValue  } },
        createdAt = try {
            Date.from(Instant.parse(createdAt))
        } catch (_: DateTimeParseException) {
            Date()
        },
        isNSFW = isNSFW,
        isPolitical = isPolitical,
        views = views,
        commentCount = commentCount,
        upVotes = upVotes,
        downVotes = downVotes,
        attachments = attachments?.map { it.toModel() },
        myCommentProfile = myCommentProfile?.toModel(),
        isMine = isMine,
        comments = comments?.map { it.toModel() }?.toMutableList() ?: mutableListOf(),
        content = content,
        myVote = myVote,
        myScrap = myScrap != null,
        scrapID = myScrap?.id
    )
}
