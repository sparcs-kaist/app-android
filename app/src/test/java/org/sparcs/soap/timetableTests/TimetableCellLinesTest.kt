package org.sparcs.soap.timetableTests

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.features.timetable.components.TimetableCellLines
import org.sparcs.soap.app.features.timetable.components.TimetableCellMetrics

class TimetableCellLinesTest {
    private fun lines(
        height: Dp,
        maxTitleLines: Int = 3,
        maxSubtitleLines: Int = 0,
        maxRoomLines: Int = 2,
        titleLineHeight: Dp = 16.dp,
        detailLineHeight: Dp = 13.dp,
    ) = TimetableCellLines.of(
        height = height,
        titleLineHeight = titleLineHeight,
        detailLineHeight = detailLineHeight,
        spacing = 2.dp,
        maxTitleLines = maxTitleLines,
        maxSubtitleLines = maxSubtitleLines,
        maxRoomLines = maxRoomLines,
    )

    @Test
    fun `too short for a second line keeps only the title`() {
        val result = lines(height = 28.dp)
        assertEquals(0, result.room)
        assertEquals(1, result.title)
    }

    @Test
    fun `the second line goes to the room and closes up the spacing`() {
        val result = lines(height = 30.dp)
        assertEquals(1, result.room)
        assertEquals(1, result.title)
        assertEquals(1.dp, result.spacing)
    }

    @Test
    fun `the subtitle comes before the room's second line`() {
        val result = lines(height = 46.dp, maxSubtitleLines = 1)
        assertEquals(1, result.subtitle)
        assertEquals(1, result.room)
    }

    @Test
    fun `the room's second line comes before more title`() {
        val result = lines(height = 44.dp)
        assertEquals(2, result.room)
        assertEquals(1, result.title)
    }

    @Test
    fun `the title takes what is left up to its limit`() {
        val result = lines(height = 200.dp, maxSubtitleLines = 3)
        assertEquals(3, result.subtitle)
        assertEquals(2, result.room)
        assertEquals(3, result.title)
    }

    @Test
    fun `no room keeps every line for the title`() {
        val result = lines(height = 60.dp, maxRoomLines = 0, maxSubtitleLines = 1)
        assertEquals(0, result.room)
        assertEquals(0, result.subtitle)
        assertEquals(3, result.title)
    }

    @Test
    fun `zero and negative sizes fall back to a single title line`() {
        val zero = lines(height = 0.dp, titleLineHeight = 0.dp, detailLineHeight = 0.dp)
        val negative = lines(height = (-10).dp)
        assertEquals(1, zero.title)
        assertEquals(0, zero.room)
        assertEquals(1, negative.title)
        assertEquals(0, negative.room)
    }

    @Test
    fun `narrow or short cells are packed`() {
        assertTrue(TimetableCellMetrics.of(71.dp, 100.dp).isPacked)
        assertTrue(TimetableCellMetrics.of(100.dp, 59.dp).isPacked)
        assertFalse(TimetableCellMetrics.of(72.dp, 60.dp).isPacked)
        assertEquals(1, TimetableCellMetrics.of(50.dp, 100.dp).roomMaxLines)
    }
}
