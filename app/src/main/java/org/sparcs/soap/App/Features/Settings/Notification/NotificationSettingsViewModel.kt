package org.sparcs.soap.App.Presentation.Settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Helpers.FeatureType
import org.sparcs.soap.App.Domain.Usecases.FCMUseCaseProtocol
import org.sparcs.soap.R
import javax.inject.Inject

interface NotificationSettingsViewModelProtocol {

}
@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fcmUseCase: FCMUseCaseProtocol
) : ViewModel(), NotificationSettingsViewModelProtocol {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("fcm_settings", Context.MODE_PRIVATE)
    }

    private val _toggleState = mutableStateMapOf<FeatureType, Boolean>()
    val toggleState: Map<FeatureType, Boolean> = _toggleState

    private val _isAlertPresented = MutableStateFlow(false)
    val isAlertPresented = _isAlertPresented.asStateFlow()

    private val _alertTitle = MutableStateFlow("")
    val alertTitle = _alertTitle.asStateFlow()

    private val _alertMessage = MutableStateFlow("")
    val alertMessage = _alertMessage.asStateFlow()

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
            } catch (e: Exception) {
                _alertTitle.value = context.getString(R.string.error_update_failed_title)
                _alertMessage.value = e.localizedMessage ?: context.getString(R.string.unexpected_error)
                _isAlertPresented.value = true
            }
        }
    }

    private fun updateToggleState(service: FeatureType, isActive: Boolean) {
        try {
            prefs.edit().putBoolean("fcm.${service.rawValue}", isActive).apply()
            _toggleState[service] = isActive
        } catch (e: Exception) {
            _alertTitle.value = context.getString(R.string.error_save_failed_title)
            _alertMessage.value = context.getString(R.string.error_encode_failed_message)
            _isAlertPresented.value = true
        }
    }

    fun dismissAlert() {
        _isAlertPresented.value = false
    }
}