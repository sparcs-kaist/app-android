package com.example.soap.Features.TaxiPreview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.BuildConfig
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
    private val taxiRoomUseCase: TaxiRoomUseCaseProtocol
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
        destination: LatLng
    ): List<LatLng> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY

            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${source.latitude},${source.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&mode=transit" +
                    "&key=${apiKey}"

            val result = URL(url).readText()
            val json = JSONObject(result)
            val points = mutableListOf<LatLng>()
            val routes = json.getJSONArray("routes")
            if (routes.length() > 0) {
                val overviewPolyline = routes.getJSONObject(0)
                    .getJSONObject("overview_polyline")
                    .getString("points")
                points.addAll(decodePolyline(overviewPolyline))
            }
            points
        } catch (e: Exception) {
            Log.e("TaxiPreviewViewModel", "Error calculating route points: ${e.message}")
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

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dLat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dLng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }

        return poly
    }

}
