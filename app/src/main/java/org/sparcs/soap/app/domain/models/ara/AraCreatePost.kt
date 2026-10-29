package org.sparcs.soap.app.domain.models.ara

import org.sparcs.soap.app.domain.enums.ara.AraPostNicknameType

data class AraCreatePost(
    val title: String,
    val content: String,
    val attachmentIds: List<Int>,
    val topic: AraBoardTopic?,
    val isNSFW: Boolean,
    val isPolitical: Boolean,
    val nicknameType: AraPostNicknameType,
    val board: AraBoard,
    val marketListing: AraMarketListing? = null,
)
