package org.sparcs.soap.App.Domain.Models.Ara

import org.sparcs.soap.App.Domain.Enums.Ara.AraPostNicknameType

data class AraCreatePost(
    val title: String,
    val content: String,
    val attachments: List<AraAttachment>,
    val topic: AraBoardTopic?,
    val isNSFW: Boolean,
    val isPolitical: Boolean,
    val nicknameType: AraPostNicknameType,
    val board: AraBoard
)