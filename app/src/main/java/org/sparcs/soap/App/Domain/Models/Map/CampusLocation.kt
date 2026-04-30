package org.sparcs.soap.App.Domain.Models.Map

import com.kakao.vectormap.LatLng
import org.sparcs.soap.App.Domain.Enums.Map.LocationCategory
import java.util.UUID


data class CampusLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: LocationCategory,
    val id: UUID? = UUID.randomUUID(),
) {
    companion object

    val coordinate: LatLng
        get() = LatLng.from(latitude, longitude)
}