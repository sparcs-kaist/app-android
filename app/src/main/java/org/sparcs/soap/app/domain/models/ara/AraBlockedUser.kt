package org.sparcs.soap.app.domain.models.ara

import java.net.URL

data class AraBlockedUser(
    val id: Int,
    val nickname: String,
    val profilePictureURL: URL?,
)

object AraHiddenReason {
    const val BLOCKED_USER_CONTENT = "BLOCKED_USER_CONTENT"
}

val AraPost.isHiddenForBlockedAuthor: Boolean
    get() = AraHiddenReason.BLOCKED_USER_CONTENT in hiddenReason

val AraPostComment.isHiddenForBlockedAuthor: Boolean
    get() = hiddenReason?.contains(AraHiddenReason.BLOCKED_USER_CONTENT) == true
