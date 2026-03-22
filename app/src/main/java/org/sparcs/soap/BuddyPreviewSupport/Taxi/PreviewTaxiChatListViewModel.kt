package org.sparcs.soap.BuddyPreviewSupport.Taxi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.soap.App.Features.TaxiChatList.TaxiChatListViewModel
import org.sparcs.soap.App.Features.TaxiChatList.TaxiChatListViewModelProtocol
import org.sparcs.soap.App.Shared.Mocks.Taxi.mock
import org.sparcs.soap.App.Shared.Mocks.Taxi.mockList

class PreviewTaxiChatListViewModel(
    initialState: TaxiChatListViewModel.ViewState = TaxiChatListViewModel.ViewState.Loaded(
        emptyList(),
        emptyList()
    ),
) : TaxiChatListViewModelProtocol {

    // MARK: - Properties
    override var taxiUser: TaxiUser? by mutableStateOf(TaxiUser.mock())

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<TaxiChatListViewModel.ViewState> = _state.asStateFlow()

    // MARK: - Functions
    override suspend fun fetchData() {
        _state.value =
            TaxiChatListViewModel.ViewState.Loaded(TaxiRoom.mockList(), TaxiRoom.mockList())
    }
}