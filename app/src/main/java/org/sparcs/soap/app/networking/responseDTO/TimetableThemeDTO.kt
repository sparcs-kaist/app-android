package org.sparcs.soap.app.networking.responseDTO

import org.sparcs.soap.app.domain.helpers.TimetableTheme
import java.util.UUID

data class TimetableThemeDTO(
    val name: String,
    val hexColors: List<String>,
    val textColorHex: String,
    val separatorColorHex: String? = null,
    val backgroundColorHex: String? = null,
    val gridLabelColorHex: String? = null,
) {
    fun toModel(): TimetableTheme = TimetableTheme(
        id = "custom.${UUID.randomUUID()}", name = requireNotNull(name), hexColors = requireNotNull(hexColors),
        textColorHex = requireNotNull(textColorHex), separatorColorHex = separatorColorHex,
        backgroundColorHex = backgroundColorHex, gridLabelColorHex = gridLabelColorHex
    ).also { require(it.isValid) { "Invalid shared theme" } }

    companion object {
        fun fromModel(theme: TimetableTheme) = TimetableThemeDTO(
            theme.name, theme.hexColors, theme.textColorHex, theme.separatorColorHex,
            theme.backgroundColorHex, theme.gridLabelColorHex
        )
    }
}

data class TimetableThemeShareResponseDTO(val code: String, val theme: TimetableThemeDTO)
