package com.sparcs.soap.Shared.ViewModelMocks

import com.sparcs.soap.Features.Settings.SettingsViewModelProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class MockSettingsViewModel : SettingsViewModelProtocol {

    private val _darkModeSetting = MutableStateFlow(false)
    override val darkModeSetting: StateFlow<Boolean?> = _darkModeSetting.asStateFlow()

    override fun setTheme(mode: String) {}
    override fun signOut() {}
    override fun handleException(error: Throwable) {}
}