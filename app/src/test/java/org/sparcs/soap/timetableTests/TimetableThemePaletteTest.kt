package org.sparcs.soap.timetableTests

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.features.settings.timetable.TimetableThemePalette

class TimetableThemePaletteTest {
    private val palette = TimetableThemePalette.from(listOf("112233", "445566", "112233"))

    @Test
    fun duplicateColorsKeepDistinctIdentitiesDuringEditingAndReordering() {
        val color = palette.colors.last()
        val updated = palette.update(color.id, "ABCDEF").move(color.id, -2)

        assertEquals(listOf("ABCDEF", "112233", "445566"), updated.hexColors)
        assertEquals(color.id, updated.colors.first().id)
        assertEquals(3, updated.colors.distinctBy { it.id }.size)
    }

    @Test
    fun colorsCanMoveInBothDirectionsButNotPastBoundaries() {
        val first = palette.colors.first()
        val moved = palette.move(first.id, 1)

        assertEquals(listOf("445566", "112233", "112233"), moved.hexColors)
        assertEquals(palette, moved.move(first.id, -1))
        assertEquals(palette, palette.move(first.id, -1))
        assertEquals(palette, palette.move(palette.colors.last().id, 1))
        assertEquals(palette, palette.move("missing", 1))
    }

    @Test
    fun paletteAlwaysKeepsAtLeastOneColor() {
        val remaining = palette.remove(palette.colors[0].id).remove(palette.colors[1].id)

        assertFalse(remaining.canRemove)
        assertEquals(remaining, remaining.remove(remaining.colors.single().id))
    }

    @Test
    fun colorsCanBeAddedBackAfterDeletionUpToSixteen() {
        var updated = palette.remove(palette.colors.first().id)
        repeat(20) { updated = updated.add() }

        assertEquals(TimetableTheme.maximumColors, updated.colors.size)
        assertFalse(updated.canAdd)
        assertEquals(updated, updated.add())
        assertTrue(updated.remove(updated.colors.first().id).canAdd)
    }

    @Test
    fun draftRestorationPreservesOrderAndIdentities() {
        val reordered = palette.move(palette.colors.last().id, -2)
        val restored = Json.decodeFromString<TimetableThemePalette>(Json.encodeToString(reordered))

        assertEquals(reordered, restored)
        val theme = TimetableTheme.Default.duplicate("Palette").copy(hexColors = restored.hexColors)
        assertTrue(theme.isValid)
        assertEquals(TimetableTheme.color(restored.hexColors[0]), theme.colorFor(3))
    }
}
