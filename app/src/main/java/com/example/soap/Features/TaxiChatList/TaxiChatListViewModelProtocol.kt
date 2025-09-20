package com.example.soap.Features.TaxiChatList

import com.example.soap.Domain.Models.Taxi.TaxiUser
import kotlinx.coroutines.flow.StateFlow

interface TaxiChatListViewModelProtocol {
    // MARK: - ViewModel Properties
    val state: StateFlow<TaxiChatListViewModel.ViewState>
    var taxiUser: TaxiUser?

    // MARK: - Functions
    suspend fun fetchData()
}