package org.sparcs.soap.timetableTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.ActivityDraft
import org.sparcs.soap.app.domain.models.otl.ActivityParsingError
import org.sparcs.soap.app.domain.models.otl.ActivityParsingException
import org.sparcs.soap.app.domain.models.otl.ActivityTime
import org.sparcs.soap.app.domain.models.otl.ParsedActivity
import org.sparcs.soap.app.domain.usecases.otl.ActivityResponseParser

class ParsedActivityTest {
    private fun parsed(
        title: String? = "Study group",
        location: String? = null,
        startHour: Int? = null,
        startMinute: Int? = null,
        endHour: Int? = null,
        endMinute: Int? = null,
        durationMinutes: Int? = null,
    ) = ParsedActivity.fromClock(title, location, listOf(DayType.TUE), startHour, startMinute, endHour, endMinute, durationMinutes)

    @Test
    fun `clock times become minutes from midnight`() {
        val activity = parsed(startHour = 19, startMinute = 30, endHour = 21, endMinute = 0)
        assertEquals(19 * 60 + 30, activity.begin)
        assertEquals(21 * 60, activity.end)
        assertEquals(DayType.TUE, activity.day)
    }

    @Test
    fun `missing minutes mean on the hour`() {
        val activity = parsed(startHour = 9, endHour = 10)
        assertEquals(9 * 60, activity.begin)
        assertEquals(10 * 60, activity.end)
    }

    @Test
    fun `an end before the start is read twelve hours later`() {
        val activity = parsed(startHour = 19, endHour = 9)
        assertEquals(19 * 60, activity.begin)
        assertEquals(21 * 60, activity.end)
    }

    @Test
    fun `an end that cannot fit is dropped`() {
        val activity = parsed(startHour = 22, endHour = 0)
        assertEquals(22 * 60, activity.begin)
        assertNull(activity.end)
    }

    @Test
    fun `a length is used without an end time`() {
        assertEquals(15 * 60 + 30, parsed(startHour = 14, durationMinutes = 90).end)
        assertEquals(24 * 60, parsed(startHour = 23, durationMinutes = 120).end)
    }

    @Test
    fun `an end time wins over a length`() {
        assertEquals(15 * 60, parsed(startHour = 14, endHour = 15, durationMinutes = 90).end)
    }

    @Test
    fun `times out of range are left out`() {
        val activity = parsed(startHour = 25, endHour = 26)
        assertNull(activity.begin)
        assertNull(activity.end)
        assertEquals(9 * 60 + 59, parsed(startHour = 9, startMinute = 75).begin)
    }

    @Test
    fun `an end without a start is left out`() {
        assertNull(parsed(endHour = 10).end)
    }

    @Test
    fun `blank text is left out`() {
        val activity = parsed(title = "  ", location = "\n")
        assertNull(activity.title)
        assertNull(activity.location)
        assertEquals("Gym", parsed(title = " Gym ", location = " N1 ").title)
    }

    @Test
    fun `empty only when nothing is filled in`() {
        assertTrue(ParsedActivity().isEmpty)
        assertFalse(ParsedActivity(day = DayType.MON).isEmpty)
        assertFalse(ParsedActivity(title = "Gym").isEmpty)
    }

    @Test
    fun `applying keeps what the description leaves out`() {
        val draft = ActivityDraft(title = "Old", location = "N1", day = 0, begin = 540, end = 600)
        val filled = ParsedActivity(title = "Study", day = DayType.TUE).applied(draft)
        assertEquals(ActivityDraft(title = "Study", location = "N1", day = 1, begin = 540, end = 600), filled)
    }

    @Test
    fun `a start without an end keeps the current length`() {
        val draft = ActivityDraft(title = "Gym", day = 0, begin = 540, end = 630)
        val filled = ParsedActivity(begin = 19 * 60).applied(draft)
        assertEquals(19 * 60, filled.begin)
        assertEquals(19 * 60 + 90, filled.end)
    }

    @Test
    fun `applied times snap to quarter hours and stay within the day`() {
        val draft = ActivityDraft(title = "Gym", day = 0, begin = 540, end = 600)
        val snapped = ParsedActivity(begin = 19 * 60 + 7, end = 20 * 60 + 52).applied(draft)
        assertEquals(19 * 60, snapped.begin)
        assertEquals(20 * 60 + 45, snapped.end)
        val late = ParsedActivity(begin = 23 * 60 + 55).applied(draft)
        assertEquals(24 * 60 - 15, late.begin)
        assertEquals(24 * 60, late.end)
        assertTrue(late.end - late.begin >= 15)
    }

    @Test
    fun `scanner days and times override the model`() {
        val response = """{"title":"스터디","location":"도서관","days":["Monday"],"startHour":7,"endHour":9}"""
        val activity = ActivityResponseParser.parse(response, "화요일 저녁 7–9시 도서관에서 스터디", DayType.SUN)
        assertEquals("스터디", activity.title)
        assertEquals("도서관", activity.location)
        assertEquals(DayType.TUE, activity.day)
        assertEquals(19 * 60, activity.begin)
        assertEquals(21 * 60, activity.end)
    }

    @Test
    fun `model values fill in what the scanner misses`() {
        val response = """Sure! {"title":"Gym","location":null,"days":["Thursday"],"startHour":18,"durationMinutes":60}"""
        val activity = ActivityResponseParser.parse(response, "Gym on thursday evening", DayType.SUN)
        assertEquals(DayType.THU, activity.day)
        assertEquals(18 * 60, activity.begin)
        assertEquals(19 * 60, activity.end)
    }

    @Test
    fun `a length from the model applies to a scanned start`() {
        val response = """{"title":"운동","durationMinutes":120}"""
        val activity = ActivityResponseParser.parse(response, "저녁 7시부터 2시간 운동", DayType.SUN)
        assertEquals(19 * 60, activity.begin)
        assertEquals(21 * 60, activity.end)
    }

    @Test
    fun `an unreadable answer still uses the scanner`() {
        val activity = ActivityResponseParser.parse("not json", "월요일 21-22시", DayType.SUN)
        assertNull(activity.title)
        assertEquals(DayType.MON, activity.day)
        assertEquals(21 * 60, activity.begin)
    }

    @Test
    fun `nothing found is an error`() {
        val error = runCatching { ActivityResponseParser.parse("{}", "hello", DayType.SUN) }.exceptionOrNull()
        assertEquals(ActivityParsingError.NOTHING_FOUND, (error as ActivityParsingException).reason)
    }

    @Test
    fun `several days become a time on each day`() {
        val draft = ActivityDraft(title = "Gym", day = 0, begin = 540, end = 600)
        val filled = ParsedActivity(days = listOf(DayType.MON, DayType.WED, DayType.FRI), begin = 22 * 60).applied(draft)
        assertEquals(listOf(0, 2, 4), filled.times.map { it.day })
        assertTrue(filled.times.all { it.begin == 22 * 60 && it.end == 23 * 60 })
    }

    @Test
    fun `several hours on one day become a time each`() {
        val draft = ActivityDraft(title = "Gym", day = 0, begin = 540, end = 600)
        val filled = ParsedActivity(
            days = listOf(DayType.WED),
            begin = 7 * 60,
            laterTimes = listOf(ParsedActivity.Time(21 * 60)),
        ).applied(draft)
        assertEquals(listOf(ActivityTime(2, 7 * 60, 8 * 60), ActivityTime(2, 21 * 60, 22 * 60)), filled.times)
    }

    @Test
    fun `a single time keeps the other times already entered`() {
        val draft = ActivityDraft(title = "Gym", times = listOf(ActivityTime(0, 540, 600), ActivityTime(3, 600, 660)))
        val filled = ParsedActivity(begin = 19 * 60).applied(draft)
        assertEquals(listOf(ActivityTime(0, 19 * 60, 20 * 60), ActivityTime(3, 600, 660)), filled.times)
    }

    @Test
    fun `every day reads as all seven days with every scanned time`() {
        val activity = ActivityResponseParser.parse("{}", "매일 아침 7시, 저녁 9시 운동", DayType.SUN)
        assertEquals(7, activity.days.size)
        assertEquals(7 * 60, activity.begin)
        assertEquals(listOf(21 * 60), activity.laterTimes.map { it.begin })
    }
}
