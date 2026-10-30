package org.sparcs.soap.postTests

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.enums.ara.AraPostNicknameType
import org.sparcs.soap.app.domain.models.ara.AraMarketListing
import org.sparcs.soap.app.domain.models.ara.AraUpdatePost
import org.sparcs.soap.app.features.postList.components.postListRow.formattedPrice
import org.sparcs.soap.app.networking.requestDTO.ara.AraPostUpdateRequestDTO
import org.sparcs.soap.app.networking.responseDTO.ara.AraMarketMetadataDTO
import java.util.Locale

class AraMarketMetadataTest {

    private val gson = Gson()

    private fun parse(json: String): AraMarketListing? = gson.fromJson(json, AraMarketMetadataDTO::class.java)?.toModel()

    @Test
    fun readsPricesWrittenInDifferentShapes() {
        assertEquals(12_000L, parse("""{"price":12000,"state":"onsale"}""")?.price)
        assertEquals(12_000L, parse("""{"price":"12,000"}""")?.price)
        assertEquals(12_000L, parse("""{"price":12000.7}""")?.price)
        assertNull(parse("""{"price":1e300,"state":"soldout"}""")?.price)
    }

    @Test
    fun defaultsCurrencyAndStateAndIgnoresOtherMetadata() {
        val listing = parse("""{"price":5000}""")!!
        assertEquals("KRW", listing.currency)
        assertEquals(AraMarketListing.State.ON_SALE, listing.state)
        assertEquals(AraMarketListing.State.RESERVED, parse("""{"state":"reserved"}""")?.state)
        assertNull(parse("""{"something_else":true}"""))
    }

    @Test
    fun leavesOutMetadataWhenAnEditDoesNotChangeIt() {
        val json = gson.toJson(AraPostUpdateRequestDTO.fromModel(AraUpdatePost("t", "{}", emptyList(), false, false)))
        assertFalse(json.contains("metadata"))
        val market = gson.toJson(AraPostUpdateRequestDTO.fromModel(AraUpdatePost("t", "{}", emptyList(), false, false, AraMarketListing(3000))))
        assertTrue(market.contains("\"price_currency\":\"KRW\""))
    }

    @Test
    fun formatsPricesInTheirCurrency() {
        assertEquals("15,000원", AraMarketListing(15_000).formattedPrice(Locale.KOREA))
        assertNull(AraMarketListing(null).formattedPrice(Locale.KOREA))
    }

    @Test
    fun readsTheNameFlagsABoardAllows() {
        assertEquals(listOf(AraPostNicknameType.REGULAR, AraPostNicknameType.ANONYMOUS), AraPostNicknameType.allowed(3))
        assertEquals(listOf(AraPostNicknameType.REALNAME), AraPostNicknameType.allowed(4))
        assertEquals(AraPostNicknameType.ANONYMOUS, AraPostNicknameType.fromCode(2))
        assertNull(AraPostNicknameType.fromCode(8))
    }
}
