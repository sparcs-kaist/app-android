package org.sparcs.soap.taxiFavoritesTests

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.sparcs.soap.app.domain.helpers.TaxiFavoritesStore
import org.sparcs.soap.app.domain.models.taxi.TaxiFavoriteRoute
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.domain.repositories.taxi.TaxiUserRepositoryProtocol
import org.sparcs.soap.app.domain.usecases.taxi.TaxiFavoriteUseCase
import org.sparcs.soap.app.domain.usecases.taxi.resolve
import org.sparcs.soap.app.shared.mocks.taxi.mockList

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class TaxiFavoritesTest {
    private val preferences get() = RuntimeEnvironment.getApplication()
        .getSharedPreferences("taxi_favorites_test", Context.MODE_PRIVATE)
    private val locations = TaxiLocation.mockList()
    private val route get() = TaxiFavoriteRoute("server-id", locations[0], locations[1])
    private val repository = FakeRepository()
    private fun store() = TaxiFavoritesStore(preferences)
    private fun useCase() = TaxiFavoriteUseCase(repository, store())

    @Before fun reset() { preferences.edit().clear().commit() }

    @Test fun `places persist locally while routes load from server`() = runTest {
        val favorites = useCase()
        favorites.togglePlace(locations[0])
        repository.routes = listOf(route)
        val reloaded = useCase()
        assertEquals(listOf(locations[0].id), reloaded.placeIDs.value)
        assertTrue(reloaded.favoriteRoutes.value.isEmpty())
        reloaded.fetchFavoriteRoutes()
        assertEquals(listOf(route), reloaded.favoriteRoutes.value)
        reloaded.togglePlace(locations[0])
        assertTrue(store().placeIDs.isEmpty())
    }

    @Test fun `saving refreshes server ids and prevents duplicates but preserves direction`() = runTest {
        val favorites = useCase()
        favorites.addFavoriteRoute(locations[0].id, locations[0].id)
        favorites.addFavoriteRoute(locations[0].id, locations[1].id)
        favorites.addFavoriteRoute(locations[0].id, locations[1].id)
        favorites.addFavoriteRoute(locations[1].id, locations[0].id)
        assertEquals(2, repository.creates)
        assertEquals(repository.routes, favorites.favoriteRoutes.value)
        favorites.deleteFavoriteRoute(favorites.favoriteRoutes.value.first().id)
        assertEquals("server-1", repository.deletedID)
        assertEquals(repository.routes, favorites.favoriteRoutes.value)
    }

    @Test fun `repeated save taps create only one route`() = runTest {
        val favorites = useCase()
        repository.gate = CompletableDeferred()
        val first = launch { favorites.addFavoriteRoute(locations[0].id, locations[1].id) }
        val second = launch { favorites.addFavoriteRoute(locations[0].id, locations[1].id) }
        runCurrent()
        repository.gate!!.complete(Unit)
        first.join()
        second.join()
        assertEquals(1, repository.creates)
    }

    @Test fun `failed fetch preserves routes and cancellation propagates`() = runTest {
        val favorites = useCase()
        repository.routes = listOf(route)
        favorites.fetchFavoriteRoutes()
        repository.fetchError = IllegalStateException("offline")
        favorites.fetchFavoriteRoutes()
        assertEquals(listOf(route), favorites.favoriteRoutes.value)
        repository.fetchError = CancellationException("cancelled")
        try {
            favorites.fetchFavoriteRoutes()
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }

    @Test fun `failed mutations propagate without changing cached routes`() = runTest {
        val favorites = useCase()
        repository.routes = listOf(route)
        favorites.fetchFavoriteRoutes()
        repository.mutationError = IllegalStateException("offline")
        try {
            favorites.deleteFavoriteRoute(route.id)
            fail("Deletion must fail")
        } catch (_: IllegalStateException) { }
        assertEquals(listOf(route), favorites.favoriteRoutes.value)
    }

    @Test fun `logout clears local data and ignores pending server response`() = runTest {
        val favorites = useCase()
        favorites.togglePlace(locations[0])
        preferences.edit().putString("taxi.favoriteRoutes", "legacy").commit()
        repository.routes = listOf(route)
        repository.gate = CompletableDeferred()
        val pending = launch { favorites.fetchFavoriteRoutes() }
        runCurrent()
        favorites.clear()
        repository.gate!!.complete(Unit)
        pending.join()
        assertTrue(favorites.favoriteRoutes.value.isEmpty())
        assertTrue(useCase().placeIDs.value.isEmpty())
        assertFalse(preferences.contains("taxi.favoriteRoutes"))
        assertEquals(listOf(route), repository.routes)
    }

    @Test fun `resolution refreshes locations and removes missing duplicate or same-end routes`() {
        val routes = listOf(route, route.copy(id = "duplicate"),
            route.copy(id = "missing", to = locations[1].copy(id = "removed")),
            route.copy(id = "same", to = locations[0]))
        assertEquals(listOf(route), routes.resolve(locations))
    }

    @Test fun `corrupt places read as empty`() {
        preferences.edit().putString("taxi.favoritePlaces", "42").commit()
        assertTrue(store().placeIDs.isEmpty())
    }

    private inner class FakeRepository : TaxiUserRepositoryProtocol {
        var routes = emptyList<TaxiFavoriteRoute>()
        var creates = 0
        var deletedID: String? = null
        var gate: CompletableDeferred<Unit>? = null
        var fetchError: Exception? = null
        var mutationError: Exception? = null
        override suspend fun fetchFavoriteRoutes(): List<TaxiFavoriteRoute> {
            val snapshot = routes
            gate?.await()
            fetchError?.let { throw it }
            return snapshot
        }
        override suspend fun createFavoriteRoute(fromId: String, toId: String) {
            mutationError?.let { throw it }
            creates++
            routes = routes + TaxiFavoriteRoute("server-$creates",
                locations.first { it.id == fromId }, locations.first { it.id == toId })
        }
        override suspend fun deleteFavoriteRoute(id: String) {
            mutationError?.let { throw it }
            deletedID = id
            routes = routes.filterNot { it.id == id }
        }
        override suspend fun fetchUser(): TaxiUser = error("Unused")
        override suspend fun editBadge(showBadge: Boolean) {}
        override suspend fun editNickname(nickname: String) {}
        override suspend fun editBankAccount(account: String) {}
        override suspend fun registerPhoneNumber(phoneNumber: String) {}
        override suspend fun registerResidence(residence: String) {}
    }
}
