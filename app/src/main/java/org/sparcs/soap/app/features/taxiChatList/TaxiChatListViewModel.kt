package org.sparcs.soap.app.features.taxiChatList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.domain.repositories.taxi.TaxiRoomRepositoryProtocol
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol
import javax.inject.Inject
import org.sparcs.soap.app.domain.services.MockTaxiUnreadStore
import org.sparcs.soap.app.domain.services.TaxiUnreadStoreProtocol

interface TaxiChatListViewModelProtocol {
    // MARK: - ViewModel Properties
    val state: StateFlow<TaxiChatListViewModel.ViewState>
    /** Unread chats by ride ID. Rides with none may be missing. */
    val unreadCounts: StateFlow<Map<String, Int>>
    var taxiUser: TaxiUser?

    // MARK: - Functions
    fun fetchData()
}

@HiltViewModel
class TaxiChatListViewModel @Inject constructor(
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol,
    private val userUseCase: UserUseCaseProtocol,
    taxiUnreadStore: TaxiUnreadStoreProtocol = MockTaxiUnreadStore(),
) : ViewModel(), TaxiChatListViewModelProtocol {

    override val unreadCounts: StateFlow<Map<String, Int>> = taxiUnreadStore.counts

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(val onGoing: List<TaxiRoom>, val done: List<TaxiRoom>) : ViewState()
        data class Error(val error: Exception) : ViewState()
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    //MARK: - viewModel Properties
    override var taxiUser: TaxiUser? = null

    init {
        fetchTaxiUser()
    }

    //MARK: - Functions
    override fun fetchData() {
        viewModelScope.launch {
            try {
                val (onGoingRooms, doneRooms) = taxiRoomRepository.fetchMyRooms()
                _state.value = ViewState.Loaded(
                    onGoing = onGoingRooms,
                    done = doneRooms
                )
            } catch (e: Exception) {
                _state.value = ViewState.Error(e)
            }
        }
    }

    private fun fetchTaxiUser() {
        taxiUser = userUseCase.taxiUser
    }
}