package org.sparcs.soap.app.features.taxiPreview

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
import org.sparcs.soap.R
import org.sparcs.soap.app.cache.TaxiRouteCache
import org.sparcs.soap.app.domain.enums.taxi.TaxiRoomBlockStatus
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiRoute
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.domain.repositories.taxi.TaxiRoomRepositoryProtocol
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.taxi.TaxiRoomUseCaseProtocol
import org.sparcs.soap.app.shared.extensions.toAlertState
import timber.log.Timber
import javax.inject.Inject

interface TaxiPreviewViewModelProtocol {
    val taxiUser: StateFlow<TaxiUser?>
    val blockStatus: StateFlow<TaxiRoomBlockStatus>

    var alertState: AlertState?
    var isAlertPresented: Boolean

    fun isJoined(participants: List<TaxiParticipant>): Boolean
    suspend fun calculateRoute(source: LatLng, destination: LatLng): TaxiRoute?
    suspend fun joinRoom(id: String): TaxiRoom?
}


@HiltViewModel
class TaxiPreviewViewModel @Inject constructor(
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol,
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
        viewModelScope.launch {
            loadInitialData()
        }
    }

    private suspend fun loadInitialData() {
        try {
            if (userUseCase.taxiUser == null) {
                userUseCase.fetchTaxiUser()
            }
            _taxiUser.value = userUseCase.taxiUser
        } catch (e: Exception) {
            Timber.e(e, "Taxi user fetch failed")
        }

        _blockStatus.value = taxiRoomUseCase.isBlocked()
    }

    // MARK: - Logic
    override fun isJoined(participants: List<TaxiParticipant>): Boolean {
        return participants.any { it.id == _taxiUser.value?.oid }
    }

    override suspend fun calculateRoute(
        source: LatLng,
        destination: LatLng,
    ): TaxiRoute? = withContext(Dispatchers.IO) {
        val cacheKey =
            "route_v2_${source.latitude},${source.longitude}_${destination.latitude},${destination.longitude}"

        taxiRouteCache.getRoute(cacheKey)?.let { return@withContext it }

        try {
            val url = Constants.MAPS_URL +
                    "origin=${source.longitude},${source.latitude}" +
                    "&destination=${destination.longitude},${destination.latitude}"

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "KakaoAK ${Constants.KAKAO_NAVI_KEY}")
                .get()
                .build()

            OkHttpClient().newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (!response.isSuccessful || responseBody == null) {
                    Timber.e("HTTP error: ${response.code}")
                    return@withContext null
                }
                parseRoute(JSONObject(responseBody))?.also { taxiRouteCache.store(cacheKey, it) }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing route: ${e.message}")
            null
        }
    }

    private fun parseRoute(json: JSONObject): TaxiRoute? {
        val route = json.optJSONArray("routes")?.optJSONObject(0) ?: return null
        val summary = route.optJSONObject("summary") ?: return null
        val sections = route.optJSONArray("sections") ?: return null

        val points = buildList {
            for (i in 0 until sections.length()) {
                val roads = sections.getJSONObject(i).optJSONArray("roads") ?: continue
                for (j in 0 until roads.length()) {
                    val vertexes = roads.getJSONObject(j).optJSONArray("vertexes") ?: continue
                    for (k in 0 until vertexes.length() - 1 step 2) {
                        add(LatLng.from(vertexes.getDouble(k + 1), vertexes.getDouble(k)))
                    }
                }
            }
        }
        if (points.isEmpty()) return null

        return TaxiRoute(
            points = points,
            distanceMeters = summary.optInt("distance"),
            durationSeconds = summary.optInt("duration"),
        )
    }

    override suspend fun joinRoom(id: String): TaxiRoom? {
        return try {
            taxiRoomRepository.joinRoom(id)
        } catch (e: Exception) {
            Timber.e("Error joining room: ${e.message}")
            alertState = e.toAlertState(R.string.error_failed_to_join_taxi_room)
            isAlertPresented = true
            null
        }
    }
}
