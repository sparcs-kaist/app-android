package com.example.soap.Shared.ViewModelMocks

import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Domain.Models.Taxi.TaxiUser
import com.example.soap.Features.TaxiChatList.TaxiChatListViewModel
import com.example.soap.Features.TaxiChatList.TaxiChatListViewModelProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockTaxiChatListViewModel(initialState: TaxiChatListViewModel.ViewState) : TaxiChatListViewModelProtocol{

    //MARL: - ViewModel Properties
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<TaxiChatListViewModel.ViewState> = _state

    override var taxiUser: TaxiUser? = null

    //MARK: - Functions
    override suspend fun fetchData() {}
}