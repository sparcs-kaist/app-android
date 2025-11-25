package com.sparcs.soap.Domain.Helpers

import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashlyticsHelper @Inject constructor() {

    private val _showAlert = MutableStateFlow(false)
    val showAlert: StateFlow<Boolean> get() = _showAlert

    private val _alertMessage = MutableStateFlow("")
    val alertMessage: StateFlow<String> get() = _alertMessage

    fun recordException(
        error: Throwable,
        showAlert: Boolean = true,
        alertMessage: String = "Something went wrong. Please try again later."
    ) {
        when (error) {
            is UnknownHostException,
            is IOException
                -> {
                _showAlert.value = showAlert
                _alertMessage.value = "You are not connected to the Internet."
                return
            }
        }

        FirebaseCrashlytics.getInstance().recordException(error)

        _showAlert.value = showAlert
        _alertMessage.value = alertMessage
    }
}
