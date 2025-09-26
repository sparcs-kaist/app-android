package com.example.soap.Features.Settings.Ara

import com.example.soap.Domain.Models.Ara.AraUser
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

interface AraSettingsViewModelProtocol {
    val state: StateFlow<AraSettingsViewModel.ViewState>
    var user: StateFlow<AraUser?>
    var allowNSFW: Boolean
    var allowPolitical: Boolean
    var nickname: String
    val nicknameUpdatable: Boolean
    val nicknameUpdatableFrom: Date?

    suspend fun fetchUser() {}
    @Throws(Exception::class)
    suspend fun updateNickname() {}
    suspend fun updateContentPreference() {}
}
