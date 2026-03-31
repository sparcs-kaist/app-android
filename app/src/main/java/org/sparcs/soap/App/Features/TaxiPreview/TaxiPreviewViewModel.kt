package org.sparcs.soap.App.Features.TaxiPreview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.vectormap.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.sparcs.soap.App.Cache.TaxiRouteCache
import org.sparcs.soap.App.Domain.Enums.Taxi.TaxiRoomBlockStatus
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Helpers.Constants
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiParticipant
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiRoomRepository
import org.sparcs.soap.App.Domain.Usecases.Taxi.TaxiRoomUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.UserUseCaseProtocol
import org.sparcs.soap.App.Shared.Extensions.toAlertState
import org.sparcs.soap.R
import timber.log.Timber
import javax.inject.Inject

interface TaxiPreviewViewModelProtocol {
    val taxiUser: StateFlow<TaxiUser?>
    val blockStatus: StateFlow<TaxiRoomBlockStatus>

    var alertState: AlertState?
    var isAlertPresented: Boolean

    fun isJoined(participants: List<TaxiParticipant>): Boolean
    suspend fun calculateRoutePoints(source: LatLng, destination: LatLng): List<LatLng>
    fun joinRoom(id: String)
}


@HiltViewModel
class TaxiPreviewViewModel @Inject constructor(
    private val taxiRoomRepository: TaxiRoomRepository,
    private val userUseCase: UserUseCaseProtocol,
    private val taxiRoomUseCase: TaxiRoomUseCaseProtocol,
    private val taxiRouteCache: TaxiRouteCache,
) : ViewModel(), TaxiPreviewViewModelProtocol {

    // MARK: - Properties
    private val _taxiUser = MutableStateFlow<TaxiUser?>(null)
    override val taxiUser: StateFlow<TaxiUser?> = _taxiUser

    private val _blockStatus = MutableStateFlow<TaxiRoomBlockStatus>(TaxiRoomBlockStatus.Allow)
    override val blockStatus: StateFlow<TaxiRoomBlockStatus> = _blockStatus

    override var alertState by mutableStateOf<AlertState?>(null)
    override var isAlertPresented by mutableStateOf(false)

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
        source: LatLng,
        destination: LatLng,
    ): List<LatLng> = withContext(Dispatchers.IO) {
        val cacheKey =
            "route_${source.latitude},${source.longitude}_${destination.latitude},${destination.longitude}"

        taxiRouteCache.getRoute(cacheKey)?.let {
            Timber.d("TaxiRoute Cache Hit! key: $cacheKey")
            return@withContext it
        }

        try {
            val client = OkHttpClient()

            val url = Constants.mapsURL +
                    "origin=${source.longitude},${source.latitude}" +
                    "&destination=${destination.longitude},${destination.latitude}"

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "KakaoAK ${Constants.KAKAO_NAVI_KEY}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody == null) {
                    Timber.e("HTTP error: ${response.code}")
                    return@withContext emptyList()
                }

                val json = JSONObject(responseBody)
                val routes = json.optJSONArray("routes") ?: return@withContext emptyList()
                val sections = routes.getJSONObject(0).optJSONArray("sections") ?: return@withContext emptyList()

                val points = mutableListOf<LatLng>()
                for (i in 0 until sections.length()) {
                    val roads = sections.getJSONObject(i).optJSONArray("roads") ?: continue
                    for (j in 0 until roads.length()) {
                        val vertexes = roads.getJSONObject(j).optJSONArray("vertexes") ?: continue
                        for (k in 0 until vertexes.length() step 2) {
                            val lon = vertexes.getDouble(k)
                            val lat = vertexes.getDouble(k + 1)
                            points.add(LatLng.from(lat, lon))
                        }
                    }
                }

                if (points.isNotEmpty()) {
                    taxiRouteCache.store(cacheKey, points)
                    Timber.d("TaxiRoute Cache Stored! key: $cacheKey")
                }

                points
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing route: ${e.message}")
            listOf(source, destination)
        }
    }

    override fun joinRoom(id: String) {
        viewModelScope.launch {
            try {
                taxiRoomRepository.joinRoom(id)
            } catch (e: Exception) {
                Timber.e("Error joining room: ${e.message}")
                alertState = e.toAlertState(R.string.error_failed_to_join_taxi_room)
                isAlertPresented = true
                throw e
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
