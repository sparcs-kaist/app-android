package org.sparcs.soap.taxiFavoritesTests

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.sparcs.soap.app.domain.helpers.TaxiFavoritesStore
import org.sparcs.soap.app.domain.models.taxi.TaxiFavoriteRoute
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import org.sparcs.soap.app.domain.usecases.taxi.TaxiFavoriteUseCase
import org.sparcs.soap.app.domain.usecases.taxi.resolve
import org.sparcs.soap.app.shared.mocks.taxi.mockList

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class TaxiFavoritesTest {
    private val preferences: SharedPreferences
        get() = RuntimeEnvironment.getApplication().getSharedPreferences("taxi_favorites_test", Context.MODE_PRIVATE)
    private val locations = TaxiLocation.mockList()

    private fun store() = TaxiFavoritesStore(preferences)

    private fun location(index: Int, id: String = "place-$index") = locations[0].copy(id = id)

    @Before fun reset() { preferences.edit().clear().commit() }

    @Test fun `favourites survive recreating the store`() {
        val useCase = TaxiFavoriteUseCase(store())
        useCase.togglePlace(locations[0])
        useCase.saveRoute(locations[0], locations[1])

        val reloaded = TaxiFavoriteUseCase(store())
        assertEquals(listOf(locations[0].id), reloaded.placeIDs.value)
        assertEquals(listOf(TaxiFavoritesStore.Route(locations[0].id, locations[1].id)), reloaded.routes.value)
    }

    @Test fun `starring a place twice removes it`() {
        val useCase = TaxiFavoriteUseCase(store())
        useCase.togglePlace(locations[2])
        useCase.togglePlace(locations[2])
        assertTrue(useCase.placeIDs.value.isEmpty())
        assertTrue(store().placeIDs.isEmpty())
    }

    @Test fun `routes are newest first, unique, directional and capped`() {
        val useCase = TaxiFavoriteUseCase(store())
        useCase.saveRoute(locations[0], locations[1])
        useCase.saveRoute(locations[0], locations[1])
        useCase.saveRoute(locations[1], locations[0])
        assertEquals(
            listOf(TaxiFavoritesStore.Route(locations[1].id, locations[0].id), TaxiFavoritesStore.Route(locations[0].id, locations[1].id)),
            useCase.routes.value
        )

        repeat(12) { useCase.saveRoute(location(it, "from-$it"), location(it, "to-$it")) }
        assertEquals(TaxiFavoriteUseCase.MAXIMUM_ROUTES, useCase.routes.value.size)
        assertEquals("from-11", useCase.routes.value.first().sourceID)
    }

    @Test fun `the same place at both ends is never saved`() {
        val useCase = TaxiFavoriteUseCase(store())
        useCase.saveRoute(locations[0], locations[0])
        assertTrue(useCase.routes.value.isEmpty())
    }

    @Test fun `removing a resolved route forgets it`() {
        val useCase = TaxiFavoriteUseCase(store())
        useCase.saveRoute(locations[0], locations[1])
        useCase.removeRoute(TaxiFavoriteRoute("any", locations[0], locations[1]))
        assertTrue(useCase.routes.value.isEmpty())
        assertTrue(store().routes.isEmpty())
    }

    @Test fun `routes to places that no longer exist drop out`() {
        val routes = listOf(
            TaxiFavoritesStore.Route(locations[0].id, locations[1].id),
            TaxiFavoritesStore.Route(locations[0].id, "removed"),
        )
        val resolved = routes.resolve(locations)
        assertEquals(1, resolved.size)
        assertEquals(locations[1], resolved.single().to)
    }

    @Test fun `signing out clears every favourite`() {
        val useCase = TaxiFavoriteUseCase(store())
        useCase.togglePlace(locations[0])
        useCase.saveRoute(locations[0], locations[1])
        useCase.clear()

        assertTrue(useCase.placeIDs.value.isEmpty())
        assertTrue(useCase.routes.value.isEmpty())
        val reloaded = TaxiFavoriteUseCase(store())
        assertTrue(reloaded.placeIDs.value.isEmpty())
        assertTrue(reloaded.routes.value.isEmpty())
    }

    @Test fun `corrupt stored data reads as no favourites`() {
        preferences.edit().putString("taxi.favoriteRoutes", "{broken").putString("taxi.favoritePlaces", "42").commit()
        assertTrue(store().routes.isEmpty())
        assertTrue(store().placeIDs.isEmpty())
    }
}
