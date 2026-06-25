package org.sparcs.soap.app.domain.models.taxi

data class SenderInfo(
    val id: String?,
    val name: String?,
    val avatarURL: String?,
    val isMine: Boolean,
    val isWithdrew: Boolean
)