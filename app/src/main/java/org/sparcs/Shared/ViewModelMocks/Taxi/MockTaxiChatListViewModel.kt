package org.sparcs.Shared.ViewModelMocks.Taxi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.Domain.Models.Taxi.TaxiUser
import org.sparcs.Features.TaxiChatList.TaxiChatListViewModel
import org.sparcs.Features.TaxiChatList.TaxiChatListViewModelProtocol

class MockTaxiChatListViewModel(initialState: TaxiChatListViewModel.ViewState) :
    TaxiChatListViewModelProtocol {

    //MARK: - ViewModel Properties
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<TaxiChatListViewModel.ViewState> = _state

    override var taxiUser: TaxiUser? = null

    //MARK: - Functions
    override suspend fun fetchData() {}
}