package org.sparcs.soap.app.domain.models.ara

import java.util.Date

data class AraUser(
    val id: Int,
    val nickname: String,
    val nicknameUpdatedAt: Date?,
    val allowNSFW: Boolean,
    val allowPolitical: Boolean
)