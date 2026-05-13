package org.sparcs.soap.App.Domain.Usecases.Taxi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiFavoriteRoute
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiUserRepositoryProtocol
import javax.inject.Inject
import javax.inject.Singleton

interface TaxiFavoriteUseCaseProtocol {
    val favoriteRoutes: StateFlow<List<TaxiFavoriteRoute>>
    suspend fun fetchFavoriteRoutes()
    suspend fun addFavoriteRoute(fromId: String, toId: String)
    suspend fun deleteFavoriteRoute(id: String)
}

@Singleton
class TaxiFavoriteUseCase @Inject constructor(
    private val taxiUserRepository: TaxiUserRepositoryProtocol
) : TaxiFavoriteUseCaseProtocol {

    private val _favoriteRoutes = MutableStateFlow<List<TaxiFavoriteRoute>>(emptyList())
    override val favoriteRoutes: StateFlow<List<TaxiFavoriteRoute>> = _favoriteRoutes.asStateFlow()

    override suspend fun fetchFavoriteRoutes() {
        try {
            _favoriteRoutes.value = taxiUserRepository.fetchFavoriteRoutes()
        } catch (e: Exception) {
            // Log error
        }
    }

    override suspend fun addFavoriteRoute(fromId: String, toId: String) {
        try {
            taxiUserRepository.createFavoriteRoute(fromId, toId)
        } finally {
            fetchFavoriteRoutes()
        }
    }

    override suspend fun deleteFavoriteRoute(id: String) {
        try {
            taxiUserRepository.deleteFavoriteRoute(id)
        } finally {
            fetchFavoriteRoutes()
        }
    }
}

class MockTaxiFavoriteUseCase : TaxiFavoriteUseCaseProtocol {
    override val favoriteRoutes: StateFlow<List<TaxiFavoriteRoute>> = MutableStateFlow(emptyList<TaxiFavoriteRoute>()).asStateFlow()
    override suspend fun fetchFavoriteRoutes() {}
    override suspend fun addFavoriteRoute(fromId: String, toId: String) {}
    override suspend fun deleteFavoriteRoute(id: String) {}
}
