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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    /** Fetches this device's preferences again, such as to retry after an error. */
    fun load()
    fun updateSystemPermission(isAllowed: Boolean)
    /** Turns a kind on or off at once, and back with an alert if the change can't be saved. */
    fun setNotifications(category: NotificationCategory, enabled: Boolean)
    fun dismissAlert()
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

    /** The values last loaded from or saved to the server, which a failed change goes back to. */
    private val savedPreferences = mutableMapOf<NotificationCategory, Boolean>()
    /** The latest change to each kind, so a change that a newer one replaces isn't saved or undone. */
    private val latestChange = mutableMapOf<NotificationCategory, Int>()
    /** Changes are saved one at a time, in order, so the last one is what the server keeps. */
    private val saveMutex = Mutex()

    init {
        load()
    }

    override fun load() {
        viewModelScope.launch {
            // Coming back to the screen should not cover switches already shown with a spinner.
            if (_preferences.isEmpty()) _state.value = ViewState.Loading
            try {
                val loaded = fcmUseCase.notificationPreferences()
                _preferences.clear()
                _preferences.putAll(loaded)
                savedPreferences.clear()
                savedPreferences.putAll(loaded)
                _state.value = ViewState.Loaded
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to load notification preferences")
                _state.value = ViewState.Error(e)
            }
        }
    }

    override fun updateSystemPermission(isAllowed: Boolean) {
        isAllowedBySystem = isAllowed
    }

    override fun setNotifications(category: NotificationCategory, enabled: Boolean) {
        // The switch moves at once, and moves back if the change can't be saved.
        _preferences[category] = enabled
        val change = (latestChange[category] ?: 0) + 1
        latestChange[category] = change
        viewModelScope.launch {
            saveMutex.withLock {
                // Flipped again since: the newer change saves its own value.
                if (latestChange[category] != change) return@withLock
                try {
                    fcmUseCase.setNotifications(category, enabled)
                    savedPreferences[category] = enabled
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.e(e, "Failed to change notification preference")
                    if (latestChange[category] != change) return@withLock
                    val saved = savedPreferences[category]
                    if (saved == null) _preferences.remove(category) else _preferences[category] = saved
                    alertState = AlertState(
                        titleResId = R.string.notification_settings_change_failed,
                        message = e.localizedMessage,
                    )
                    isAlertPresented = true
                }
            }
        }
    }

    override fun dismissAlert() {
        isAlertPresented = false
    }
}
