package com.sparcs.soap.Shared.ViewModelMocks.Taxi

import com.sparcs.soap.Domain.Models.Taxi.TaxiUser
import com.sparcs.soap.Features.Settings.Taxi.TaxiSettingsViewModel
import com.sparcs.soap.Features.Settings.Taxi.TaxiSettingsViewModelProtocol
import com.sparcs.soap.Shared.Mocks.mock
import kotlinx.coroutines.flow.MutableStateFlow


class MockTaxiSettingsViewModel(initialState: TaxiSettingsViewModel.ViewState): TaxiSettingsViewModelProtocol {

    override var bankName: String? = "카카오뱅크"
    override var bankNumber: String = "3333-01-1234567"
    override var phoneNumber: String = "010-0000-0000"
    override var residence: String = "기숙사"

    override var user: TaxiUser? = TaxiUser.mock()

    override val state = MutableStateFlow(initialState)

    override suspend fun fetchUser() {}

    override suspend fun editBankAccount(account: String) {}
    override suspend fun registerPhoneNumber(phoneNumber: String) {}
    override suspend fun registerResidence(residence: String) {}
}