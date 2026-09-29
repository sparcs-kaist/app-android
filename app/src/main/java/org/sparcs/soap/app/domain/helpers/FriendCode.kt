package org.sparcs.soap.app.domain.helpers

import java.util.Locale

/**
 * Friend invite codes are six ASCII letters/digits, upper-cased — the same
 * shape as timetable theme share codes.
 */
object FriendCode {
    const val LENGTH = 6

    fun normalized(input: String): String? = input.trim()
        .takeIf { it.matches(Regex("[A-Za-z0-9]{$LENGTH}")) }
        ?.uppercase(Locale.ROOT)

    /**
     * Keeps only what a code can contain, so anything else never reaches the
     * field rather than being rejected after the fact.
     */
    fun sanitized(input: String): String = input
        .filter { it in 'A'..'Z' || it in 'a'..'z' || it in '0'..'9' }
        .take(LENGTH)
        .uppercase(Locale.ROOT)
}
