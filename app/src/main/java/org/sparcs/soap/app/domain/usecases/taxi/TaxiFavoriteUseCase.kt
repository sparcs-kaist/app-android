package org.sparcs.soap.app.domain.usecases.taxi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.sparcs.soap.app.domain.helpers.TaxiFavoritesStore
import org.sparcs.soap.app.domain.models.taxi.TaxiFavoriteRoute
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import org.sparcs.soap.app.domain.repositories.taxi.TaxiUserRepositoryProtocol
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface TaxiFavoriteUseCaseProtocol {
    val placeIDs: StateFlow<List<String>>
    val favoriteRoutes: StateFlow<List<TaxiFavoriteRoute>>

    fun togglePlace(location: TaxiLocation)
    suspend fun fetchFavoriteRoutes()
    suspend fun addFavoriteRoute(fromId: String, toId: String)
    suspend fun deleteFavoriteRoute(id: String)
    fun clear()
}

fun List<TaxiFavoriteRoute>.resolve(locations: List<TaxiLocation>): List<TaxiFavoriteRoute> {
    val byID = locations.associateBy { it.id }
    return mapNotNull { route ->
        val from = byID[route.from.id] ?: return@mapNotNull null
        val to = byID[route.to.id] ?: return@mapNotNull null
        route.copy(from = from, to = to).takeIf { from.id != to.id }
    }.distinctBy { it.from.id to it.to.id }
}

fun List<TaxiFavoriteRoute>.containsRoute(source: TaxiLocation, destination: TaxiLocation): Boolean =
    any { it.from.id == source.id && it.to.id == destination.id }

@Singleton
class TaxiFavoriteUseCase @Inject constructor(
    private val taxiUserRepository: TaxiUserRepositoryProtocol,
    private val store: TaxiFavoritesStore,
) : TaxiFavoriteUseCaseProtocol {

    private val _placeIDs = MutableStateFlow(store.placeIDs)
    override val placeIDs: StateFlow<List<String>> = _placeIDs.asStateFlow()

    private val _favoriteRoutes = MutableStateFlow<List<TaxiFavoriteRoute>>(emptyList())
    override val favoriteRoutes: StateFlow<List<TaxiFavoriteRoute>> = _favoriteRoutes.asStateFlow()
    private val routeMutex = Mutex()
    private var session = 0

    override fun togglePlace(location: TaxiLocation) {
        val ids = _placeIDs.value
        _placeIDs.value = if (location.id in ids) ids - location.id else ids + location.id
        store.placeIDs = _placeIDs.value
    }

    override suspend fun fetchFavoriteRoutes() {
        val currentSession = session
        routeMutex.withLock { refreshRoutes(currentSession) }
    }

    private suspend fun refreshRoutes(currentSession: Int) {
        if (currentSession != session) return
        try {
            val routes = taxiUserRepository.fetchFavoriteRoutes()
            if (currentSession == session) _favoriteRoutes.value = routes
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "fetchFavoriteRoutes failed")
        }
    }

    override suspend fun addFavoriteRoute(fromId: String, toId: String) {
        val currentSession = session
        routeMutex.withLock {
            if (currentSession != session || fromId == toId ||
                _favoriteRoutes.value.any { it.from.id == fromId && it.to.id == toId }) return
            taxiUserRepository.createFavoriteRoute(fromId, toId)
            refreshRoutes(currentSession)
        }
    }

    override suspend fun deleteFavoriteRoute(id: String) {
        val currentSession = session
        routeMutex.withLock {
            if (currentSession != session) return
            taxiUserRepository.deleteFavoriteRoute(id)
            refreshRoutes(currentSession)
        }
    }

    override fun clear() {
        session++
        store.clear()
        _placeIDs.value = emptyList()
        _favoriteRoutes.value = emptyList()
    }
}

class MockTaxiFavoriteUseCase(
    placeIDs: List<String> = emptyList(),
    favoriteRoutes: List<TaxiFavoriteRoute> = emptyList(),
) : TaxiFavoriteUseCaseProtocol {
    private val _placeIDs = MutableStateFlow(placeIDs)
    override val placeIDs: StateFlow<List<String>> = _placeIDs.asStateFlow()

    private val _favoriteRoutes = MutableStateFlow(favoriteRoutes)
    override val favoriteRoutes: StateFlow<List<TaxiFavoriteRoute>> = _favoriteRoutes.asStateFlow()

    override fun togglePlace(location: TaxiLocation) {
        val ids = _placeIDs.value
        _placeIDs.value = if (location.id in ids) ids - location.id else ids + location.id
    }

    override suspend fun fetchFavoriteRoutes() {}
    override suspend fun addFavoriteRoute(fromId: String, toId: String) {}
    override suspend fun deleteFavoriteRoute(id: String) {}

    override fun clear() {
        _placeIDs.value = emptyList()
        _favoriteRoutes.value = emptyList()
    }
}
