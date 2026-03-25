package org.sparcs.soap.App.Features.TaxiChatList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiRoomRepositoryProtocol
import org.sparcs.soap.App.Domain.Usecases.UserUseCaseProtocol
import javax.inject.Inject

interface TaxiChatListViewModelProtocol {
    // MARK: - ViewModel Properties
    val state: StateFlow<TaxiChatListViewModel.ViewState>
    var taxiUser: TaxiUser?

    // MARK: - Functions
    suspend fun fetchData()
}

@HiltViewModel
class TaxiChatListViewModel @Inject constructor(
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol,
    private val userUseCase: UserUseCaseProtocol,
) : ViewModel(), TaxiChatListViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(val onGoing: List<TaxiRoom>, val done: List<TaxiRoom>) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    //MARK: - viewModel Properties
    override var taxiUser: TaxiUser? = null

    init {
        fetchTaxiUser()
    }

    //MARK: - Functions
    override suspend fun fetchData() {
        viewModelScope.launch {
            try {
                val (onGoingRooms, doneRooms) = taxiRoomRepository.fetchMyRooms()
                _state.value = ViewState.Loaded(
                    onGoing = onGoingRooms,
                    done = doneRooms
                )
            } catch (e: Exception) {
                _state.value = ViewState.Error(
                    message = e.localizedMessage ?: "Unknown error"
                )
            }
        }
    }

    private fun fetchTaxiUser() {
        taxiUser = userUseCase.taxiUser
    }
}