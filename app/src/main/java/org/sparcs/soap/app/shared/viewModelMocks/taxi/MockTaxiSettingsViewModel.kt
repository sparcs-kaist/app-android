package org.sparcs.soap.app.shared.viewModelMocks.taxi

import kotlinx.coroutines.flow.MutableStateFlow
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.features.settings.taxi.TaxiSettingsViewModel
import org.sparcs.soap.app.features.settings.taxi.TaxiSettingsViewModelProtocol
import org.sparcs.soap.app.shared.mocks.taxi.mock


class MockTaxiSettingsViewModel(initialState: TaxiSettingsViewModel.ViewState):
    TaxiSettingsViewModelProtocol {

    override var bankName: String? = "카카오뱅크"
    override var bankNumber: String = "3333-01-1234567"
    override var phoneNumber: String = "010-0000-0000"
    override var showBadge: Boolean = true
    override var residence: String = "기숙사"

    override val alertState: AlertState? = null
    override var isAlertPresented: Boolean = false

    override var user: TaxiUser? = TaxiUser.mock()

    override val state = MutableStateFlow(initialState)

    override suspend fun fetchUser() {}
    override suspend fun editInformation(): Boolean { return false }
}