package com.sparcs.soap.Shared.ViewModelMocks

import com.sparcs.soap.Domain.Models.Taxi.TaxiUser
import com.sparcs.soap.Features.TaxiChatList.TaxiChatListViewModel
import com.sparcs.soap.Features.TaxiChatList.TaxiChatListViewModelProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockTaxiChatListViewModel(initialState: TaxiChatListViewModel.ViewState) : TaxiChatListViewModelProtocol{

    //MARK: - ViewModel Properties
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<TaxiChatListViewModel.ViewState> = _state

    override var taxiUser: TaxiUser? = null

    //MARK: - Functions
    override suspend fun fetchData() {}
}