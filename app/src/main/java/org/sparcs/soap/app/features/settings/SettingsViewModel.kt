package org.sparcs.soap.app.features.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.error.auth.AuthUseCaseError
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.repositories.settings.SettingsRepository
import org.sparcs.soap.app.domain.services.CrashlyticsService
import org.sparcs.soap.app.domain.usecases.AuthUseCaseProtocol
import org.sparcs.soap.app.shared.extensions.toAlertState
import javax.inject.Inject

interface SettingsViewModelProtocol {
    val darkModeSetting: StateFlow<Boolean?>

    val alertState: AlertState?
    var isAlertPresented: Boolean

    fun setTheme(mode: String)
    fun signOut()
    fun handleException(error: Throwable)
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val crashlyticsService: CrashlyticsService,
    private val authUseCase: AuthUseCaseProtocol,
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


    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    override fun setTheme(mode: String) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    override fun signOut() {
        viewModelScope.launch {
            try {
                authUseCase.signOut()
            } catch (e: Exception) {
                val authError = e as? AuthUseCaseError
                alertState = e.toAlertState(
                    authError?.messageRes ?: R.string.unexpected_error_signing_out
                )
                isAlertPresented = true
                handleException(e)
            }
        }
    }


    override fun handleException(error: Throwable) {
        crashlyticsService.recordException(error)
    }
}