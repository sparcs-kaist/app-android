package org.sparcs.soap.buddyPreviewSupport.taxi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.features.taxiChatList.TaxiChatListViewModel
import org.sparcs.soap.app.features.taxiChatList.TaxiChatListViewModelProtocol
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.mocks.taxi.mockList

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
    override fun fetchData() {
        _state.value =
            TaxiChatListViewModel.ViewState.Loaded(TaxiRoom.mockList(), TaxiRoom.mockList())
    }
}