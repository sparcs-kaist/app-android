package org.sparcs.soap.App.Domain.Models.Ara

import java.util.Date

data class AraUser(
    val id: Int,
    val nickname: String,
    val nicknameUpdatedAt: Date?,
    val allowNSFW: Boolean,
    val allowPolitical: Boolean
)