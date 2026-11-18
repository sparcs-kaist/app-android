package org.sparcs.soap.taxiListTests

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.sparcs.soap.app.domain.helpers.LocalizedString
import org.sparcs.soap.app.domain.models.taxi.TaxiFavoriteRoute
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import org.sparcs.soap.app.features.main.LocalAnalytics
import org.sparcs.soap.app.features.taxiList.TaxiListView
import org.sparcs.soap.app.features.taxiList.TaxiListViewModelProtocol
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.taxi.PreviewTaxiListViewModel
import org.sparcs.soap.buddyPreviewSupport.taxi.PreviewTaxiPreviewViewModel
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-w320dp-h720dp-port")
class TaxiFavoritesPortraitTest {
    @get:Rule val compose = createComposeRule()

    @Test fun rowSpansScreenAndLastRouteStopsInsideEndMargin() {
        val places = TaxiLocation.mockList()
        val longName = "Daejeon Station East Entrance across from the Bus Terminal"
        val terminal = places.last().copy(title = LocalizedString(mapOf("en" to longName, "ko" to longName)))
        val routes = (1..3).map { TaxiFavoriteRoute(it.toString(), places[0], places[it]) } +
            TaxiFavoriteRoute("last", places[3], terminal)
        val model = object : TaxiListViewModelProtocol by PreviewTaxiListViewModel() {
            override val favoriteRoutes = MutableStateFlow(routes)
        }
        compose.setContent {
            CompositionLocalProvider(LocalAnalytics provides MockAnalyticsService()) {
                Theme { TaxiListView(model, PreviewTaxiPreviewViewModel(), rememberNavController()) }
            }
        }
        val row = compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange) and hasScrollToIndexAction())
        val screen = compose.onRoot().getUnclippedBoundsInRoot()
        val viewport = row.getUnclippedBoundsInRoot()
        assertEquals(screen.left, viewport.left)
        assertEquals(screen.right, viewport.right)
        row.performScrollToIndex(routes.lastIndex)
        val last = compose.onNodeWithContentDescription(longName, substring = true)
        last.assertIsDisplayed()
        val chip = last.getUnclippedBoundsInRoot()
        assertTrue(chip.left >= viewport.left + 15.dp)
        assertTrue(chip.bottom - chip.top > 40.dp) // The long destination wraps inside the chip.
        assertEquals((viewport.right - 16.dp).value, chip.right.value, 1f)
    }
}
