package com.example.soap.Features.TaxiRoomCreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Enums.TaxiRoomBlockStatus
import com.example.soap.Domain.Usecases.TaxiRoomUseCaseProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaxiRoomCreationViewModel @Inject constructor(
    private val taxiRoomUseCase: TaxiRoomUseCaseProtocol
) : ViewModel() {

    // MARK: - Properties
    private val _blockStatus = MutableStateFlow<TaxiRoomBlockStatus>(TaxiRoomBlockStatus.Allow)
    val blockStatus: StateFlow<TaxiRoomBlockStatus> = _blockStatus

    // MARK: - Functions
    fun fetchBlockStatus() {
        viewModelScope.launch {
            _blockStatus.value = taxiRoomUseCase.isBlocked()
        }
    }
}