package org.sparcs.soap.App.Domain.Models.Taxi

data class SenderInfo(
    val id: String?,
    val name: String?,
    val avatarURL: String?,
    val isMine: Boolean,
    val isWithdrew: Boolean
)