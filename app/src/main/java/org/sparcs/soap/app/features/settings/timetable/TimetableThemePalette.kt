package org.sparcs.soap.app.features.settings.timetable

import kotlinx.serialization.Serializable
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import java.util.UUID

@Serializable
internal data class PaletteColor(val id: String, val hex: String)

@Serializable
internal data class TimetableThemePalette(val colors: List<PaletteColor>) {
    val canAdd: Boolean get() = colors.size < TimetableTheme.maximumColors
    val canRemove: Boolean get() = colors.size > TimetableTheme.minimumColors
    val hexColors: List<String> get() = colors.map { it.hex }

    fun add(): TimetableThemePalette {
        if (!canAdd) return this
        val defaults = TimetableTheme.Default.hexColors
        val color =
            PaletteColor(UUID.randomUUID().toString(), defaults[colors.size % defaults.size])
        return copy(colors = colors + color)
    }

    fun update(id: String, hex: String): TimetableThemePalette =
        copy(colors = colors.map { if (it.id == id) it.copy(hex = hex) else it })

    fun remove(id: String): TimetableThemePalette =
        if (canRemove) copy(colors = colors.filterNot { it.id == id }) else this

    fun move(id: String, offset: Int): TimetableThemePalette {
        val source = colors.indexOfFirst { it.id == id }
        val destination = source + offset
        if (source !in colors.indices || destination !in colors.indices) return this
        val reordered = colors.toMutableList()
        reordered.add(destination, reordered.removeAt(source))
        return copy(colors = reordered)
    }

    companion object {
        fun from(hexColors: List<String>): TimetableThemePalette = TimetableThemePalette(
            hexColors.map { PaletteColor(UUID.randomUUID().toString(), it) }
        )
    }
}
