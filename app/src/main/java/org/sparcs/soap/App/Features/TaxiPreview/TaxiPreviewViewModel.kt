package org.sparcs.soap.App.Features.TaxiPreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import org.sparcs.soap.App.Domain.Enums.Taxi.TaxiRoomBlockStatus
import org.sparcs.soap.App.Domain.Helpers.Constants
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiParticipant
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiRoomRepository
import org.sparcs.soap.App.Domain.Usecases.Taxi.TaxiRoomUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.UserUseCase
import org.sparcs.soap.BuildConfig
import timber.log.Timber
import javax.inject.Inject

interface TaxiPreviewViewModelProtocol {
    val taxiUser: StateFlow<TaxiUser?>
    val blockStatus: StateFlow<TaxiRoomBlockStatus>

    fun isJoined(participants: List<TaxiParticipant>): Boolean
    suspend fun calculateRoutePoints(source: GeoPoint, destination: GeoPoint): List<GeoPoint>
    fun joinRoom(id: String)
}


@HiltViewModel
class TaxiPreviewViewModel @Inject constructor(
    private val taxiRoomRepository: TaxiRoomRepository,
    private val userUseCase: UserUseCase,
    private val taxiRoomUseCase: TaxiRoomUseCaseProtocol,
) : ViewModel(), TaxiPreviewViewModelProtocol {

    // MARK: - Properties
    private val _taxiUser = MutableStateFlow<TaxiUser?>(null)
    override val taxiUser: StateFlow<TaxiUser?> = _taxiUser

    private val _blockStatus = MutableStateFlow<TaxiRoomBlockStatus>(TaxiRoomBlockStatus.Allow)
    override val blockStatus: StateFlow<TaxiRoomBlockStatus> = _blockStatus

    // MARK: - Init
    init {
        fetchTaxiUser()
        fetchBlockStatus()
    }

    // MARK: - Logic
    override fun isJoined(participants: List<TaxiParticipant>): Boolean {
        return participants.any { it.id == _taxiUser.value?.oid }
    }

    override suspend fun calculateRoutePoints(
        source: GeoPoint,
        destination: GeoPoint,
    ): List<GeoPoint> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.MAPS_API_KEY
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
                Timber.e("HTTP error: ${response.code}")
                return@withContext emptyList()
            }

            val json = JSONObject(response.body?.string() ?: "{}")

            if (json.has("error")) {
                val errorMsg = json.getJSONObject("error").optString("message", "Unknown API error")
                Timber.e("OpenRouteService API error: $errorMsg")
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
            Timber.e(e, "Error parsing route: ${e.message}")
            emptyList()
        }
    }


    override fun joinRoom(id: String) {
        viewModelScope.launch {
            try {
                taxiRoomRepository.joinRoom(id)
            } catch (e: Exception) {
                Timber.e("Error joining room: ${e.message}")
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
