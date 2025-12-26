package org.sparcs.Domain.Usecases

import org.sparcs.Domain.Models.Taxi.TaxiLocation
import org.sparcs.Domain.Repositories.Taxi.TaxiRoomRepositoryProtocol
import org.sparcs.Shared.Mocks.mockList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

interface TaxiLocationUseCaseProtocol {
    val locations: StateFlow<List<TaxiLocation>>

    suspend fun fetchLocations()
    fun queryLocation(query: String): List<TaxiLocation>
}

class TaxiLocationUseCase @Inject constructor(
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol
): TaxiLocationUseCaseProtocol {

    private val _locations = MutableStateFlow(emptyList<TaxiLocation>())
    override val locations = _locations

    override suspend fun fetchLocations() {
        _locations.value = taxiRoomRepository.fetchLocations()
    }

    override fun queryLocation(query: String): List<TaxiLocation> {
     return _locations.value.filter { it.title.contains(query) }
    }
}

class MockTaxiLocationUseCase: TaxiLocationUseCaseProtocol {
    private val _locations = MutableStateFlow(emptyList<TaxiLocation>())
    override val locations = _locations

    override suspend fun fetchLocations() {}
    override fun queryLocation(query: String): List<TaxiLocation> {
        return TaxiLocation.mockList()
    }
}