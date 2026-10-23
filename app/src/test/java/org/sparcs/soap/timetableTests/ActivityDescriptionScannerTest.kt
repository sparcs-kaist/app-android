package org.sparcs.soap.timetableTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.helpers.ActivityDescriptionScanner
import org.sparcs.soap.app.domain.helpers.ScannedTime

class ActivityDescriptionScannerTest {
    private val week = listOf(DayType.MON, DayType.TUE, DayType.WED, DayType.THU, DayType.FRI, DayType.SAT, DayType.SUN)

    private fun times(text: String): ScannedTime? = ActivityDescriptionScanner.times(text)

    @Test
    fun `named days`() {
        listOf(
            "월요일 21-22시 동안 SPARCS 정모" to DayType.MON,
            "매주 화요일 저녁 7–9시 도서관에서 스터디" to DayType.TUE,
            "Study group every Wednesday 7 to 9pm" to DayType.WED,
            "Gym on Thursdays at 6pm" to DayType.THU,
            "Fri 10am club meeting" to DayType.FRI,
            "Band practice Sat 2pm" to DayType.SAT,
        ).forEach { (text, day) -> assertEquals(text, day, ActivityDescriptionScanner.day(text, DayType.SUN)) }
    }

    @Test
    fun `relative days count from today`() {
        assertEquals(DayType.SUN, ActivityDescriptionScanner.day("오늘 저녁 운동", DayType.SUN))
        assertEquals(DayType.MON, ActivityDescriptionScanner.day("Coffee tomorrow at 3pm", DayType.SUN))
        assertEquals(DayType.SAT, ActivityDescriptionScanner.day("내일 스터디", DayType.FRI))
    }

    @Test
    fun `the first day named wins`() {
        assertEquals(DayType.TUE, ActivityDescriptionScanner.day("화요일이랑 목요일 스터디", DayType.SUN))
    }

    @Test
    fun `every named day`() {
        listOf(
            "Meeting at 22:00, everyday" to week,
            "Gym every day 7am" to week,
            "매일 밤 10시 회의" to week,
            "평일 아침 운동" to DayType.weekdays(),
            "Study on weekends 2pm" to listOf(DayType.SAT, DayType.SUN),
            "화요일이랑 목요일 스터디" to listOf(DayType.TUE, DayType.THU),
            "월수금 9시 수영" to listOf(DayType.MON, DayType.WED, DayType.FRI),
            "화/목 19:00 스터디" to listOf(DayType.TUE, DayType.THU),
            "Band practice Mon and Wed at 6pm" to listOf(DayType.MON, DayType.WED),
        ).forEach { (text, days) -> assertEquals(text, days, ActivityDescriptionScanner.days(text, DayType.SUN)) }
    }

    @Test
    fun `single days and words stay as they are`() {
        assertEquals(listOf(DayType.MON), ActivityDescriptionScanner.days("월요일 21-22시 SPARCS 정모", DayType.SUN))
        assertEquals(listOf(DayType.SUN), ActivityDescriptionScanner.days("일요일 예배", DayType.MON))
        assertTrue(ActivityDescriptionScanner.days("SPARCS 정모", DayType.MON).isEmpty())
    }

    @Test
    fun `words that only look like days are ignored`() {
        assertNull(ActivityDescriptionScanner.day("I sat in the sun for a month", DayType.MON))
        assertNull(ActivityDescriptionScanner.day("SPARCS 정모", DayType.MON))
    }

    @Test
    fun `korean hour range`() {
        assertEquals(ScannedTime(21 * 60, 22 * 60), times("월요일 21-22시 동안 SPARCS 정모"))
        assertEquals(ScannedTime(21 * 60, 22 * 60), times("21시부터 22시까지"))
        assertEquals(ScannedTime(19 * 60 + 30, 21 * 60), times("19:30~21:00 스터디"))
    }

    @Test
    fun `korean meridiems`() {
        assertEquals(ScannedTime(19 * 60, 21 * 60), times("매주 화요일 저녁 7–9시 도서관에서 스터디"))
        assertEquals(ScannedTime(21 * 60, 22 * 60), times("오후 9시-10시"))
        assertEquals(ScannedTime(10 * 60 + 30, null), times("오전 10시 반 회의"))
        assertEquals(ScannedTime(15 * 60 + 30, 17 * 60), times("오후 3시 30분부터 5시"))
    }

    @Test
    fun `english meridiems`() {
        assertEquals(ScannedTime(21 * 60, 22 * 60), times("9 PM to 10 PM"))
        assertEquals(ScannedTime(19 * 60, 21 * 60), times("Study group 7 to 9pm"))
        assertEquals(ScannedTime(11 * 60, 13 * 60), times("11am-1pm lunch"))
        assertEquals(ScannedTime(12 * 60, 13 * 60), times("12-1pm"))
        assertEquals(ScannedTime(18 * 60, null), times("Gym at 6pm"))
    }

    @Test
    fun `early bare hours are afternoon`() {
        assertEquals(ScannedTime(13 * 60, 15 * 60), times("1-3시 동아리"))
        assertEquals(ScannedTime(15 * 60, null), times("3시 커피"))
        assertEquals(ScannedTime(10 * 60, 11 * 60), times("10-11시 스터디"))
    }

    @Test
    fun `every time given`() {
        listOf(
            "수요일 아침 7시, 저녁 9시 운동" to listOf(ScannedTime(420, null), ScannedTime(1260, null)),
            "수요일 아침 저녁 9시 운동" to listOf(ScannedTime(540, null), ScannedTime(1260, null)),
            "아침이랑 저녁 9시 운동" to listOf(ScannedTime(540, null), ScannedTime(1260, null)),
            "오전 오후 3시-4시 스터디" to listOf(ScannedTime(180, 240), ScannedTime(900, 960)),
            "Gym at 7am and 9pm" to listOf(ScannedTime(420, null), ScannedTime(1260, null)),
            "월요일 9-10시, 13-14시 조교" to listOf(ScannedTime(540, 600), ScannedTime(780, 840)),
        ).forEach { (text, expected) -> assertEquals(text, expected, ActivityDescriptionScanner.allTimes(text)) }
    }

    @Test
    fun `lengths are not times`() {
        assertEquals(listOf(ScannedTime(19 * 60, null)), ActivityDescriptionScanner.allTimes("저녁 7시부터 2시간 운동"))
        assertEquals(ScannedTime(21 * 60, null), times("저녁 9시 운동"))
    }

    @Test
    fun `numbers that are not times are ignored`() {
        assertNull(times("E11 412호에서 스터디"))
        assertNull(times("SPARCS 정모 with 3 people"))
        assertEquals(ScannedTime(19 * 60, null), times("Room E11 at 7pm"))
    }
}
