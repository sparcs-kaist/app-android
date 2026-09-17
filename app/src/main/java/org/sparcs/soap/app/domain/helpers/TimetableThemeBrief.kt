package org.sparcs.soap.app.domain.helpers

import java.util.Locale

data class TimetableThemeBrief(
    val name: String? = null,
    val appearance: Appearance? = null,
    val anchorHexColors: List<String> = emptyList(),
) {
    enum class Appearance { LIGHT, DARK }

    fun theme(base: TimetableTheme): TimetableTheme? {
        val palette = TimetablePhotoPalette.fromBrief(this) ?: return null
        val title = name?.trim()?.trim('"', '\'', '“', '”', '‘', '’')?.trim()?.take(32)
        return palette.applyTo(base).copy(name = title?.takeIf { it.isNotBlank() } ?: base.name)
    }

    companion object {
        const val maximumDescriptionLength = 120

        fun normalizeHex(value: String): String? {
            val hex = value.trim().removePrefix("#")
            if (!hex.matches(Regex("[0-9a-fA-F]{3}|[0-9a-fA-F]{6}"))) return null
            return (if (hex.length == 3) hex.map { "$it$it" }.joinToString("") else hex).uppercase(Locale.ROOT)
        }
    }
}
