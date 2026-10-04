package org.sparcs.soap.app.features.settings.notification

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.helpers.FeatureType
import org.sparcs.soap.app.domain.usecases.FCMUseCaseProtocol
import javax.inject.Inject

interface NotificationSettingsViewModelProtocol {
    var alertState: AlertState?
    var isAlertPresented: Boolean
}

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val fcmUseCase: FCMUseCaseProtocol,
) : ViewModel(), NotificationSettingsViewModelProtocol {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("fcm_settings", Context.MODE_PRIVATE)
    }

    private val _toggleState = mutableStateMapOf<FeatureType, Boolean>()
    val toggleState: Map<FeatureType, Boolean> = _toggleState

    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    fun loadSettings() {
        if (_toggleState.isNotEmpty()) return

        FeatureType.entries.forEach { type ->
            val key = "fcm.${type.rawValue}"
            val status = if (prefs.contains(key)) {
                prefs.getBoolean(key, true)
            } else {
                true
            }
            updateToggleState(type, status)
        }
    }

    fun toggle(service: FeatureType, isActive: Boolean) {
        viewModelScope.launch {
            try {
                fcmUseCase.manage(service, isActive)
                updateToggleState(service, isActive)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                alertState = AlertState(
                    titleResId = R.string.error_update_failed_title,
                    messageResId = R.string.unexpected_error,
                    message = e.localizedMessage,
                )
                isAlertPresented = true
            }
        }
    }

    private fun updateToggleState(service: FeatureType, isActive: Boolean) {
        try {
            prefs.edit { putBoolean("fcm.${service.rawValue}", isActive) }
            _toggleState[service] = isActive
        } catch (_e: Exception) {
            alertState = AlertState(
                titleResId = R.string.error_save_failed_title,
                messageResId = R.string.error_encode_failed_message,
            )
            isAlertPresented = true
        }
    }

    fun dismissAlert() {
        isAlertPresented = false
    }
}