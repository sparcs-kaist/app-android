package org.sparcs.soap.app.domain.models.ara

import org.sparcs.soap.app.domain.enums.ara.AraPostNicknameType
import java.net.URL
import java.util.Date

data class AraPost(
    val id: Int,
    val isHidden: Boolean,
    val hiddenReason: List<String>,
    val overrideHidden: Boolean?,
    val topic: AraBoardTopic?,
    val board: AraBoard?,
    val title: String?,
    val author: AraPostAuthor,
    val attachmentType: AttachmentType,
    val communicationArticleStatus: CommunicationArticleStatus?,
    val createdAt: Date,
    val isNSFW: Boolean,
    val isPolitical: Boolean,
    val views: Int,
    var commentCount: Int,
    var upVotes: Int,
    var downVotes: Int,
    // for detailed
    val attachments: List<AraPostAttachment>?,
    val myCommentProfile: AraPostAuthor?,
    val isMine: Boolean?,
    var comments: MutableList<AraPostComment>,
    val content: String?,
    var myVote: Boolean?,
    var myScrap: Boolean,
    val scrapID: Int?,
    val nicknameType: AraPostNicknameType? = null,
    val marketListing: AraMarketListing? = null,
    val readStatus: ReadStatus? = null,
) {
    enum class ReadStatus(val code: String) {
        UNREAD("N"),
        READ("-"),
        UPDATED("U");

        companion object {
            fun fromCode(code: String?): ReadStatus? = entries.firstOrNull { it.code == code }
        }
    }

    val isAnonymous: Boolean
        get() = nicknameType?.let { it == AraPostNicknameType.ANONYMOUS } ?: (author.username == ANONYMOUS_USERNAME)

    val thumbnailURL: URL?
        get() = attachments?.firstOrNull { it.mimeType.startsWith("image/") }?.file

    enum class AttachmentType(val type: String) {
        NONE("NONE"),
        IMAGE("IMAGE"),
        NON_IMAGE("FILE"),
        BOTH("BOTH")
    }

    enum class CommunicationArticleStatus(val code: Int) {
        PENDING(0),
        WAITING_FOR_ANSWER(1),
        ANSWERED(2)
    }

    companion object {
        const val ANONYMOUS_USERNAME = "anonymous"
    }
}
