package org.sparcs.soap.buddyTestSupport.useCase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import org.sparcs.soap.app.domain.usecases.taxi.TaxiLocationUseCaseProtocol

class MockTaxiLocationUseCase : TaxiLocationUseCaseProtocol {

    private val _locations = MutableStateFlow<List<TaxiLocation>>(emptyList())
    override val locations: StateFlow<List<TaxiLocation>> = _locations

    var locationsToLoad: List<TaxiLocation> = emptyList()
    var fetchLocationsResult: Result<Unit> = Result.success(Unit)

    override suspend fun fetchLocations() {
        fetchLocationsResult.getOrThrow()
        _locations.value = locationsToLoad
    }

    override fun queryLocation(query: String): List<TaxiLocation> = _locations.value
}
