package com.sparcs.soap.Features.TaxiPreview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparcs.soap.Domain.Enums.Taxi.TaxiRoomBlockStatus
import com.sparcs.soap.Domain.Helpers.Constants
import com.sparcs.soap.Domain.Models.Taxi.TaxiParticipant
import com.sparcs.soap.Domain.Models.Taxi.TaxiUser
import com.sparcs.soap.Domain.Repositories.Taxi.TaxiRoomRepository
import com.sparcs.soap.Domain.Usecases.TaxiRoomUseCaseProtocol
import com.sparcs.soap.Domain.Usecases.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
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
        source: GeoPoint,
        destination: GeoPoint,
    ): List<GeoPoint> = withContext(Dispatchers.IO) {
        try {
            val apiKey = com.sparcs.soap.BuildConfig.MAPS_API_KEY
            val client = OkHttpClient()

            val url = Constants.mapsURL +
                    "api_key=$apiKey" +
                    "&start=${source.longitude},${source.latitude}" +
                    "&end=${destination.longitude},${destination.latitude}"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("TaxiPreviewViewModel", "HTTP error: ${response.code}")
                return@withContext emptyList()
            }

            val json = JSONObject(response.body?.string() ?: "{}")

            if (json.has("error")) {
                val errorMsg = json.getJSONObject("error").optString("message", "Unknown API error")
                Log.e("TaxiPreviewViewModel", "OpenRouteService API error: $errorMsg")
                return@withContext emptyList()
            }

            val features = json.optJSONArray("features") ?: return@withContext emptyList()
            if (features.length() == 0) return@withContext emptyList()

            val geometry = features.getJSONObject(0).getJSONObject("geometry")
            val coordinates = geometry.getJSONArray("coordinates")

            val points = mutableListOf<GeoPoint>()
            for (i in 0 until coordinates.length()) {
                val coord = coordinates.getJSONArray(i)
                val lon = coord.getDouble(0)
                val lat = coord.getDouble(1)
                points.add(GeoPoint(lat, lon))
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
