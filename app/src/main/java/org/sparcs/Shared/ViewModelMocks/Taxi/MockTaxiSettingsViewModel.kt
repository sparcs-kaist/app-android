package org.sparcs.Shared.ViewModelMocks.Taxi

import kotlinx.coroutines.flow.MutableStateFlow
import org.sparcs.Domain.Models.Taxi.TaxiUser
import org.sparcs.Features.Settings.Taxi.TaxiSettingsViewModel
import org.sparcs.Features.Settings.Taxi.TaxiSettingsViewModelProtocol
import org.sparcs.Shared.Mocks.mock


class MockTaxiSettingsViewModel(initialState: TaxiSettingsViewModel.ViewState):
    TaxiSettingsViewModelProtocol {

    override var bankName: String? = "카카오뱅크"
    override var bankNumber: String = "3333-01-1234567"
    override var phoneNumber: String = "010-0000-0000"
    override var showBadge: Boolean = true
    override var residence: String = "기숙사"

    override var user: TaxiUser? = TaxiUser.mock()

    override val state = MutableStateFlow(initialState)

    override suspend fun fetchUser() {}
    override suspend fun editInformation() {}
}