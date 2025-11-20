package com.sparcs.soap.Shared.ViewModelMocks

import com.sparcs.soap.Domain.Models.Ara.AraUser
import com.sparcs.soap.Features.Settings.Ara.AraSettingsViewModel
import com.sparcs.soap.Features.Settings.Ara.AraSettingsViewModelProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import java.util.Date

class MockAraSettingsViewModel(initialState: AraSettingsViewModel.ViewState) : AraSettingsViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<AraSettingsViewModel.ViewState> = _state

    private val _user = MutableStateFlow<AraUser?>(null)
    override var user: StateFlow<AraUser?> = _user

    override var allowNSFW: Boolean = false
    override var allowPolitical: Boolean = false
    override var nickname: String = "유능한 시조새_0b4c"

    override val nicknameUpdatable: Boolean
        get() = nicknameUpdatableFrom?.let { it <= Date() } ?: false

    override val nicknameUpdatableFrom: Date?
        get() = _user.value?.nicknameUpdatedAt?.let {
            Calendar.getInstance().apply { time = it; add(Calendar.MONTH, 3) }.time
        } ?: Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time

    override suspend fun fetchUser() {}

    override suspend fun updateNickname() {}

    override suspend fun updateContentPreference() {}
}