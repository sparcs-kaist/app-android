package com.sparcs.soap.Features.Settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparcs.soap.Domain.Helpers.CrashlyticsHelper
import com.sparcs.soap.Domain.Repositories.Settings.SettingsRepository
import com.sparcs.soap.Domain.Usecases.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val crashlyticsHelper: CrashlyticsHelper,
    private val authUseCase: AuthUseCase
) : ViewModel() {

    val darkModeSetting: StateFlow<Boolean?> = settingsRepository.themeMode
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

    fun setTheme(mode: String) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun signOut(){
        viewModelScope.launch{ authUseCase.signOut() }
    }

    fun handleException(error: Throwable) {
        crashlyticsHelper.recordException(error)
    }
}