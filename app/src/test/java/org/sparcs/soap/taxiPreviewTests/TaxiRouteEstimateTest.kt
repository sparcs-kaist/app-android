package org.sparcs.soap.taxiPreviewTests

import org.junit.Assert.assertEquals
import org.junit.Test
import org.sparcs.soap.app.domain.models.taxi.TaxiRoute
import java.time.Instant
import java.util.Date

class TaxiRouteEstimateTest {
    private val departAt = Date.from(Instant.parse("2026-10-30T09:00:00Z"))

    private fun arrival(durationSeconds: Int) =
        TaxiRoute.estimatedArrival(departAt, durationSeconds).toInstant().toString()

    @Test fun `adds three and a half minutes for the taxi to arrive`() {
        assertEquals("2026-10-30T09:04:00Z", arrival(0))
    }

    @Test fun `rounds to the nearest minute rather than down`() {
        assertEquals("2026-10-30T09:22:00Z", arrival(18 * 60 + 9))
        assertEquals("2026-10-30T09:21:00Z", arrival(17 * 60 + 31))
        assertEquals("2026-10-30T09:22:00Z", arrival(18 * 60))
    }

    @Test fun `route uses its own duration`() {
        val route = TaxiRoute(points = emptyList(), distanceMeters = 8_900, durationSeconds = 25 * 60)
        assertEquals("2026-10-30T09:29:00Z", route.estimatedArrival(departAt).toInstant().toString())
    }
}
