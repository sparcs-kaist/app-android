package com.example.soap.Utilities.Mocks

import com.example.soap.Models.RoomInfo
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

fun RoomInfo.Companion.mock(): RoomInfo{
        return RoomInfo(
                origin = "대전",
                destination = "서산",
                name = "name1",
                occupancy = 2,
                capacity = 4,
                departureTime = Date.from(Instant.now().plus(1, ChronoUnit.MINUTES))
            )
}
fun RoomInfo.Companion.mockList(): List<RoomInfo> {

    return listOf(
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
