package org.sparcs.soap.app.features.settings.notification

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.helpers.NotificationCategory
import org.sparcs.soap.app.domain.usecases.FCMUseCaseProtocol
import timber.log.Timber
import javax.inject.Inject

interface NotificationSettingsViewModelProtocol {
    val state: StateFlow<NotificationSettingsViewModel.ViewState>
    /** Whether this device gets each kind of notification, including a change still being saved. */
    val preferences: Map<NotificationCategory, Boolean>
    /** Whether Android lets Buddy show notifications at all. */
    val isAllowedBySystem: Boolean
    var alertState: AlertState?
    var isAlertPresented: Boolean
}

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val fcmUseCase: FCMUseCaseProtocol,
) : ViewModel(), NotificationSettingsViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(val error: Exception) : ViewState()
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    private val _preferences = mutableStateMapOf<NotificationCategory, Boolean>()
    override val preferences: Map<NotificationCategory, Boolean> = _preferences

    override var isAllowedBySystem: Boolean by mutableStateOf(true)
        private set
    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    fun load() {
        viewModelScope.launch {
            // Coming back to the screen should not cover switches already shown with a spinner.
            if (_preferences.isEmpty()) _state.value = ViewState.Loading
            try {
                val loaded = fcmUseCase.notificationPreferences()
                _preferences.clear()
                _preferences.putAll(loaded)
                _state.value = ViewState.Loaded
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to load notification preferences")
                _state.value = ViewState.Error(e)
            }
        }
    }

    fun updateSystemPermission(isAllowed: Boolean) {
        isAllowedBySystem = isAllowed
    }

    fun setNotifications(category: NotificationCategory, enabled: Boolean) {
        // The switch moves at once, and moves back if the change can't be saved.
        val previous = _preferences[category]
        _preferences[category] = enabled
        viewModelScope.launch {
            try {
                fcmUseCase.setNotifications(category, enabled)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to change notification preference")
                if (previous == null) _preferences.remove(category) else _preferences[category] = previous
                alertState = AlertState(
                    titleResId = R.string.notification_settings_change_failed,
                    message = e.localizedMessage,
                )
                isAlertPresented = true
            }
        }
    }

    fun dismissAlert() {
        isAlertPresented = false
    }
}
