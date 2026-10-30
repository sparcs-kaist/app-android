package org.sparcs.soap.app.domain.models.taxi

import com.kakao.vectormap.LatLng
import java.util.Date
import kotlin.math.roundToLong

data class TaxiRoute(
    val points: List<LatLng>,
    val distanceMeters: Int,
    val durationSeconds: Int,
) {
    fun estimatedArrival(departAt: Date): Date = estimatedArrival(departAt, durationSeconds)

    companion object {
        const val PICKUP_WAIT_SECONDS = 210L
        private const val MINUTE_MILLIS = 60_000L

        val placeholder = TaxiRoute(points = emptyList(), distanceMeters = 8_900, durationSeconds = 18 * 60)

        fun estimatedArrival(departAt: Date, durationSeconds: Int): Date {
            val arrival = departAt.time + (durationSeconds + PICKUP_WAIT_SECONDS) * 1000
            return Date((arrival.toDouble() / MINUTE_MILLIS).roundToLong() * MINUTE_MILLIS)
        }
    }
}
