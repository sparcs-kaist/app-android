package com.example.soap.Features.Settings.OTL

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface OTLSettingsViewModelProtocol {
    var otlMajor: String
    val otlMajorList: List<String>
}

class OTLSettingsViewModel : ViewModel(), OTLSettingsViewModelProtocol {
    // MARK: - Properties
    private val _otlMajor = MutableStateFlow("School of Computer Science")
    override var otlMajor: String
        get() = _otlMajor.value
        set(value) {
            if (otlMajorList.contains(value)) {
                _otlMajor.value = value
            }
        }

    override val otlMajorList: List<String> = listOf(
        "School of Computer Science",
        "School of Electrical Engineering",
        "School of Business"
    )

    val otlMajorFlow: StateFlow<String> = _otlMajor
}
