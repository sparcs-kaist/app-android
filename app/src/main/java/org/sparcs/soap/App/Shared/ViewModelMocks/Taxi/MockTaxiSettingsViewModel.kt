package org.sparcs.soap.App.Shared.ViewModelMocks.Taxi

import kotlinx.coroutines.flow.MutableStateFlow
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.soap.App.Features.Settings.Taxi.TaxiSettingsViewModel
import org.sparcs.soap.App.Features.Settings.Taxi.TaxiSettingsViewModelProtocol
import org.sparcs.soap.App.Shared.Mocks.Taxi.mock


class MockTaxiSettingsViewModel(initialState: TaxiSettingsViewModel.ViewState):
    TaxiSettingsViewModelProtocol {

    override var bankName: String? = "카카오뱅크"
    override var bankNumber: String = "3333-01-1234567"
    override var phoneNumber: String = "010-0000-0000"
    override var showBadge: Boolean = true
    override var residence: String = "기숙사"

    override var showAlert: Boolean = false
    override var alertMessageRes: Int? = 0

    override var user: TaxiUser? = TaxiUser.mock()

    override val state = MutableStateFlow(initialState)

    override suspend fun fetchUser() {}
    override suspend fun editInformation() {}
}