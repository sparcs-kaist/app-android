package com.sparcs.soap.Features.TaxiChatList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparcs.soap.Domain.Models.Taxi.TaxiRoom
import com.sparcs.soap.Domain.Models.Taxi.TaxiUser
import com.sparcs.soap.Domain.Repositories.Taxi.TaxiRoomRepositoryProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol
): ViewModel(), TaxiChatListViewModelProtocol{

    sealed class ViewState{
        data object Loading : ViewState()
        data class Loaded(val onGoing: List<TaxiRoom>, val done: List<TaxiRoom>) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    //MARK: - viewModel Properties
    override var taxiUser: TaxiUser? = null

    //MARK: - Functions
    override suspend fun fetchData(){
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
}