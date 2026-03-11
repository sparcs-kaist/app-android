package org.sparcs.soap.App.Shared.Mocks

import org.sparcs.soap.App.Domain.Enums.Taxi.EmojiIdentifier
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiLocation
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiParticipant
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import java.net.URL
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID

fun TaxiRoom.Companion.mockList(): List<TaxiRoom>{

    val locations: List<TaxiLocation> = TaxiLocation.mockList()

    val participants = listOf(
            TaxiParticipant(
                id = "686d4d8f56fd773a8bd9d78a",
                name = "tuesday-name",
                nickname = "tuesday-nickname",
                profileImageURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/NupjukOTL.png"),
                withdraw = false,
                badge = true,
                isSettlement = null,
                readAt = Date(),
                hasCarrier = false
            ),
            TaxiParticipant(
                id = "686d4d8f56fd773a8bd9d78c",
                name = "wednesday-name",
                nickname = "wednesday-nickname",
                profileImageURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/GooseOTL.png"),
                withdraw = false,
                badge = true,
                isSettlement = null,
                readAt = Date(),
                hasCarrier = false
            )
    )

    return (0 until 18).map { index ->
        TaxiRoom(
            id = UUID.randomUUID().toString(),
            title = "Mock Room ${index + 1}",
            source = locations[index % locations.size],
            destination = locations[(index + 3) % locations.size],
            departAt = Date.from(Instant.now().plus(index.toLong(), ChronoUnit.DAYS)),
            participants = participants,
            madeAt = Date(),
            capacity = 4,
            settlementTotal = null,
            emojiIdentifier = EmojiIdentifier.APPLE,
            isDeparted = false,
            isOver = null
        )
    }
}

fun TaxiRoom.Companion.mock(): TaxiRoom {
    return TaxiRoom.mockList()[0]
}