package org.sparcs.soap.app.domain.usecases.taxi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.helpers.TaxiFavoritesStore
import org.sparcs.soap.app.domain.models.taxi.TaxiFavoriteRoute
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import javax.inject.Inject
import javax.inject.Singleton

interface TaxiFavoriteUseCaseProtocol {
    val placeIDs: StateFlow<List<String>>
    val routes: StateFlow<List<TaxiFavoritesStore.Route>>

    fun togglePlace(location: TaxiLocation)
    fun saveRoute(source: TaxiLocation, destination: TaxiLocation)
    fun removeRoute(route: TaxiFavoriteRoute)
    fun clear()
}

fun List<TaxiFavoritesStore.Route>.resolve(locations: List<TaxiLocation>): List<TaxiFavoriteRoute> {
    val byID = locations.associateBy { it.id }
    return mapNotNull { route ->
        val source = byID[route.sourceID] ?: return@mapNotNull null
        val destination = byID[route.destinationID] ?: return@mapNotNull null
        TaxiFavoriteRoute(id = "${route.sourceID}>${route.destinationID}", from = source, to = destination)
    }
}

fun List<TaxiFavoritesStore.Route>.containsRoute(source: TaxiLocation, destination: TaxiLocation): Boolean =
    any { it.sourceID == source.id && it.destinationID == destination.id }

@Singleton
class TaxiFavoriteUseCase @Inject constructor(
    private val store: TaxiFavoritesStore,
) : TaxiFavoriteUseCaseProtocol {

    private val _placeIDs = MutableStateFlow(store.placeIDs)
    override val placeIDs: StateFlow<List<String>> = _placeIDs.asStateFlow()

    private val _routes = MutableStateFlow(store.routes)
    override val routes: StateFlow<List<TaxiFavoritesStore.Route>> = _routes.asStateFlow()

    override fun togglePlace(location: TaxiLocation) {
        val ids = _placeIDs.value
        _placeIDs.value = if (location.id in ids) ids - location.id else ids + location.id
        store.placeIDs = _placeIDs.value
    }

    override fun saveRoute(source: TaxiLocation, destination: TaxiLocation) {
        if (source.id == destination.id || _routes.value.containsRoute(source, destination)) return
        val route = TaxiFavoritesStore.Route(sourceID = source.id, destinationID = destination.id)
        _routes.value = (listOf(route) + _routes.value).take(MAXIMUM_ROUTES)
        store.routes = _routes.value
    }

    override fun removeRoute(route: TaxiFavoriteRoute) {
        _routes.value = _routes.value.filterNot { it.sourceID == route.from.id && it.destinationID == route.to.id }
        store.routes = _routes.value
    }

    override fun clear() {
        store.clear()
        _placeIDs.value = emptyList()
        _routes.value = emptyList()
    }

    companion object {
        const val MAXIMUM_ROUTES = 10
    }
}

class MockTaxiFavoriteUseCase(
    placeIDs: List<String> = emptyList(),
    routes: List<TaxiFavoritesStore.Route> = emptyList(),
) : TaxiFavoriteUseCaseProtocol {
    private val _placeIDs = MutableStateFlow(placeIDs)
    override val placeIDs: StateFlow<List<String>> = _placeIDs.asStateFlow()

    private val _routes = MutableStateFlow(routes)
    override val routes: StateFlow<List<TaxiFavoritesStore.Route>> = _routes.asStateFlow()

    override fun togglePlace(location: TaxiLocation) {
        _placeIDs.value = if (location.id in _placeIDs.value) _placeIDs.value - location.id else _placeIDs.value + location.id
    }

    override fun saveRoute(source: TaxiLocation, destination: TaxiLocation) {
        if (source.id == destination.id || _routes.value.containsRoute(source, destination)) return
        _routes.value = listOf(TaxiFavoritesStore.Route(source.id, destination.id)) + _routes.value
    }

    override fun removeRoute(route: TaxiFavoriteRoute) {
        _routes.value = _routes.value.filterNot { it.sourceID == route.from.id && it.destinationID == route.to.id }
    }

    override fun clear() {
        _placeIDs.value = emptyList()
        _routes.value = emptyList()
    }
}
