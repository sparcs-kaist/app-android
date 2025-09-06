package com.example.soap.Features.TaxiPreview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Models.Taxi.TaxiParticipant
import com.example.soap.Domain.Models.Taxi.TaxiUser
import com.example.soap.Domain.Repositories.Taxi.TaxiRoomRepository
import com.example.soap.Domain.Usecases.UserUseCase
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TaxiPreviewViewModel @Inject constructor(
    private val taxiRoomRepository: TaxiRoomRepository,
    private val userUseCase: UserUseCase
) : ViewModel() {

    // MARK: - Properties
    private val _taxiUser = MutableStateFlow<TaxiUser?>(null)
    val taxiUser: StateFlow<TaxiUser?> = _taxiUser

    // MARK: - Init
    init {
        fetchTaxiUser()
    }

    // MARK: - Logic
    fun isJoined(participants: List<TaxiParticipant>): Boolean {
        return participants.any { it.id == _taxiUser.value?.oid }
    }

    fun calculateRoutePoints(
        source: LatLng,
        destination: LatLng
    ): List<LatLng> {
        return listOf(source, destination)
    }

    fun joinRoom(id: String) {
        viewModelScope.launch {
            try {
                taxiRoomRepository.joinRoom(id)
            } catch (e: Exception) {
                Log.e("TaxiPreviewViewModel", "Error joining room: ${e.message}")
            }
        }
    }

    private fun fetchTaxiUser() {
        viewModelScope.launch {
            _taxiUser.value = userUseCase.taxiUser
        }
    }

}
