package com.sparcs.soap.Domain.Models.Ara

import com.sparcs.soap.Domain.Enums.AraPostNicknameType

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