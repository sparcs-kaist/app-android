package org.sparcs.soap.App.Features.TaxiRoomCreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Enums.Taxi.TaxiRoomBlockStatus
import org.sparcs.soap.App.Domain.Usecases.Taxi.TaxiRoomUseCaseProtocol
import javax.inject.Inject

interface TaxiRoomCreationViewModelProtocol{
    val blockStatus: StateFlow<TaxiRoomBlockStatus>
    fun fetchBlockStatus()
}

@HiltViewModel
class TaxiRoomCreationViewModel @Inject constructor(
    private val taxiRoomUseCase: TaxiRoomUseCaseProtocol
) : ViewModel(), TaxiRoomCreationViewModelProtocol {

    // MARK: - Properties
    private val _blockStatus = MutableStateFlow<TaxiRoomBlockStatus>(TaxiRoomBlockStatus.Allow)
    override val blockStatus: StateFlow<TaxiRoomBlockStatus> = _blockStatus

    // MARK: - Functions
    override fun fetchBlockStatus() {
        viewModelScope.launch {
            _blockStatus.value = taxiRoomUseCase.isBlocked()
        }
    }
}