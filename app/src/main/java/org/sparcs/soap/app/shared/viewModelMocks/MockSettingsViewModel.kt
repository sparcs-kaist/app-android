package org.sparcs.soap.app.shared.viewModelMocks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.features.settings.SettingsViewModelProtocol


class MockSettingsViewModel : SettingsViewModelProtocol {

    private val _darkModeSetting = MutableStateFlow(false)
    override val darkModeSetting: StateFlow<Boolean?> = _darkModeSetting.asStateFlow()

    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    override fun setTheme(mode: String) {}
    override fun signOut() {}
    override fun handleException(error: Throwable) {}
}