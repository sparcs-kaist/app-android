package com.example.soap.Utilities.Mocks

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

data class RoomInfo(
    val origin: String,
    val destination: String,
    val name: String,
    val occupancy: Int,
    val capacity: Int,
    val departureTime: Date
) {
    companion object {
        val mock: RoomInfo
            get() = RoomInfo(
                origin = "대전",
                destination = "서산",
                name = "name1",
                occupancy = 2,
                capacity = 4,
                departureTime = Date.from(Instant.now().plus(1, ChronoUnit.MINUTES))
            )

        val mockList: List<RoomInfo>
            get() = listOf(
                RoomInfo(
                    origin = "KAIST Main Campus",
                    destination = "Seodaejeon Station",
                    name = "거위",
                    occupancy = 1,
                    capacity = 3,
                    departureTime = Date.from(Instant.now().plus(1, ChronoUnit.MINUTES))
                ),
                RoomInfo(
                    origin = "Saejong Dormitory",
                    destination = "Daejeon Terminal Complex",
                    name = "넙죽이",
                    occupancy = 3,
                    capacity = 4,
                    departureTime = Date.from(Instant.now().plus(1, ChronoUnit.DAYS))
                )
            )
    }
}
