package org.sparcs.soap.app.domain.helpers

import java.util.Locale

object TimetableThemeShareCode {
    fun normalized(input: String): String? = input.trim()
        .takeIf { it.matches(Regex("[A-Za-z0-9]{6}")) }
        ?.uppercase(Locale.ROOT)
}
