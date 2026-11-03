package org.sparcs.soap.postListTests

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.sparcs.soap.app.domain.models.ara.AraMarketListing
import org.sparcs.soap.app.features.postList.components.postListRow.formattedPrice
import org.sparcs.soap.app.features.postList.components.postListRow.priceLabel
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class MarketPriceFormattingTest {

    private fun resources(locale: Locale): Resources {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val configuration = Configuration(context.resources.configuration).apply { setLocale(locale) }
        return context.createConfigurationContext(configuration).resources
    }

    @Test
    fun formatsWonWithASymbolInEnglish() {
        assertEquals("₩20,000", AraMarketListing(20_000).formattedPrice(Locale.US))
    }

    @Test
    fun formatsWonWithASuffixInKorean() {
        assertEquals("20,000원", AraMarketListing(20_000).formattedPrice(Locale.KOREA))
    }

    @Test
    fun formatsTheLargestPriceWithoutOverflow() {
        assertEquals("₩999,999,999,999", AraMarketListing(AraMarketListing.MAX_PRICE).formattedPrice(Locale.US))
        assertEquals("₩9,223,372,036,854,775,807", AraMarketListing(Long.MAX_VALUE).formattedPrice(Locale.US))
    }

    @Test
    fun formatsOtherCurrenciesInTheirOwnFormat() {
        assertEquals("$5", AraMarketListing(5, currency = "USD").formattedPrice(Locale.US))
        assertEquals("5 XYZ", AraMarketListing(5, currency = "XYZ").formattedPrice(Locale.US))
    }

    @Test
    fun hasNoFormattedPriceWithoutAPrice() {
        assertNull(AraMarketListing(null).formattedPrice(Locale.US))
    }

    @Test
    fun labelsFreeAndMissingPrices() {
        val english = resources(Locale.US)
        val korean = resources(Locale.KOREA)
        assertEquals("Free", AraMarketListing(0).priceLabel(english, Locale.US))
        assertEquals("No price", AraMarketListing(null).priceLabel(english, Locale.US))
        assertEquals("무료 나눔", AraMarketListing(0).priceLabel(korean, Locale.KOREA))
        assertEquals("가격 없음", AraMarketListing(null).priceLabel(korean, Locale.KOREA))
        assertEquals("15,000원", AraMarketListing(15_000).priceLabel(korean, Locale.KOREA))
    }
}
