package org.sparcs.soap.app.networking.responseDTO.otl

import org.sparcs.soap.app.domain.models.otl.Friend
import org.sparcs.soap.app.domain.models.otl.FriendList
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

data class FriendDTO(
    val id: Int,
    val name: String,
    val isFavorite: Boolean,
    val hasScheduleNow: Boolean,
) {
    fun toModel(): Friend = Friend(
        id = id,
        name = name,
        isFavorite = isFavorite,
        hasScheduleNow = hasScheduleNow
    )
}

data class FriendListResponseDTO(
    val checkedAt: String?,
    val friends: List<FriendDTO>?,
) {
    fun toModel(): FriendList = FriendList(
        checkedAt = checkedAt?.let(::parseDate),
        friends = friends.orEmpty().map { it.toModel() }
    )

    // `checkedAt` comes back as ISO8601, with or without fractional seconds
    // (e.g. "2026-09-28T04:31:18.000Z").
    private fun parseDate(string: String): Instant? = try {
        OffsetDateTime.parse(string).toInstant()
    } catch (_: DateTimeParseException) {
        null
    }
}

data class FriendCodeResponseDTO(
    val code: String,
)
