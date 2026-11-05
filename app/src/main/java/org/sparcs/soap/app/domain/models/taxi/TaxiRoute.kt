package org.sparcs.soap.app.domain.models.taxi

import com.kakao.vectormap.LatLng

/**
 * A driving route between a ride's two ends: the [points] to draw, and, when the route was found
 * rather than guessed as a straight line, how far it is and how long it takes to drive.
 */
data class TaxiRoute(
    val points: List<LatLng>,
    val distanceMeters: Int? = null,
    val durationSeconds: Int? = null,
) {
    /** Whether the distance and driving time are known. */
    val hasStats: Boolean get() = distanceMeters != null && durationSeconds != null
}
