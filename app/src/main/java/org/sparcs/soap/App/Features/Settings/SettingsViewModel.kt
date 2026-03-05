package org.sparcs.soap.App.Features.Settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Repositories.Settings.SettingsRepository
import org.sparcs.soap.App.Domain.Services.CrashlyticsService
import org.sparcs.soap.App.Domain.Usecases.AuthUseCaseProtocol
import javax.inject.Inject

interface SettingsViewModelProtocol {
    val darkModeSetting: StateFlow<Boolean?>

    fun setTheme(mode: String)
    fun signOut()
    fun handleException(error: Throwable)
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val crashlyticsService: CrashlyticsService,
    private val authUseCase: AuthUseCaseProtocol
) : ViewModel(), SettingsViewModelProtocol {

    override val darkModeSetting: StateFlow<Boolean?> = settingsRepository.themeMode
        .map { mode ->
            when (mode) {
                "light" -> false
                "dark" -> true
                else -> null
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    override fun setTheme(mode: String) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    override fun signOut(){
        viewModelScope.launch{ authUseCase.signOut() }
    }

    override fun handleException(error: Throwable) {
        crashlyticsService.recordException(error)
    }
}