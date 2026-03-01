package org.sparcs.soap.App.Features.TaxiChat.Components

data class SenderInfo(
    val id: String?,
    val name: String?,
    val avatarURL: String?,
    val isMine: Boolean,
    val isWithdrew: Boolean
)