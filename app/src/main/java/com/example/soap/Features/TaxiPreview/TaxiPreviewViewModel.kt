package com.example.soap.Features.TaxiPreview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Enums.TaxiRoomBlockStatus
import com.example.soap.Domain.Models.Taxi.TaxiParticipant
import com.example.soap.Domain.Models.Taxi.TaxiUser
import com.example.soap.Domain.Repositories.Taxi.TaxiRoomRepository
import com.example.soap.Domain.Usecases.TaxiRoomUseCaseProtocol
import com.example.soap.Domain.Usecases.UserUseCase
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.inject.Inject


@HiltViewModel
class TaxiPreviewViewModel @Inject constructor(
    private val taxiRoomRepository: TaxiRoomRepository,
    private val userUseCase: UserUseCase,
    private val taxiRoomUseCase: TaxiRoomUseCaseProtocol,
) : ViewModel() {

    // MARK: - Properties
    private val _taxiUser = MutableStateFlow<TaxiUser?>(null)
    val taxiUser: StateFlow<TaxiUser?> = _taxiUser

    private val _blockStatus = MutableStateFlow<TaxiRoomBlockStatus>(TaxiRoomBlockStatus.Allow)
    val blockStatus: StateFlow<TaxiRoomBlockStatus> = _blockStatus

    // MARK: - Init
    init {
        fetchTaxiUser()
        fetchBlockStatus()
    }

    // MARK: - Logic
    fun isJoined(participants: List<TaxiParticipant>): Boolean {
        return participants.any { it.id == _taxiUser.value?.oid }
    }

    suspend fun calculateRoutePoints(
        source: LatLng,
        destination: LatLng,
    ): List<LatLng> = withContext(Dispatchers.IO) {
        try {
            val apiKey = com.example.soap.BuildConfig.MAPS_API_KEY

            val url = "https://api.openrouteservice.org/v2/directions/driving-car?" +
                    "api_key=$apiKey" +
                    "&start=${source.longitude},${source.latitude}" +
                    "&end=${destination.longitude},${destination.latitude}"

            val response = URL(url).readText()
            val json = JSONObject(response)

            val features = json.getJSONArray("features")
            if (features.length() == 0) return@withContext emptyList()

            val geometry = features.getJSONObject(0).getJSONObject("geometry")
            val coordinates = geometry.getJSONArray("coordinates")

            val points = mutableListOf<LatLng>()
            for (i in 0 until coordinates.length()) {
                val coord = coordinates.getJSONArray(i)
                val lon = coord.getDouble(0)
                val lat = coord.getDouble(1)
                points.add(LatLng(lat, lon))
            }

            points
        } catch (e: Exception) {
            Log.e("TaxiPreviewViewModel", "Error parsing route: ${e.message}", e)
            emptyList()
        }
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

    private fun fetchBlockStatus() {
        viewModelScope.launch {
            _blockStatus.value = taxiRoomUseCase.isBlocked()
        }
    }
}
