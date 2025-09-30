package com.example.soap.Shared.ViewModelMocks

import com.example.soap.Features.Settings.OTL.OTLSettingsViewModelProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class MockOTLSettingsViewModel : OTLSettingsViewModelProtocol {

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
