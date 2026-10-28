package org.sparcs.soap.timetableTests

import android.content.Context
import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.sparcs.soap.app.shared.formatters.formatHour
import org.sparcs.soap.app.shared.formatters.formatTime
import org.sparcs.soap.app.shared.formatters.formatTimeRange

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "en-rUS")
class TimeFormattersTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    private fun hourCycle(value: String) {
        Settings.System.putString(context.contentResolver, Settings.System.TIME_12_24, value)
    }

    private fun String.plainSpaces() = replace(Regex("[\\s\\u00A0\\u2009\\u202F]+"), " ")

    @Test
    fun timeFollowsTwentyFourHourSetting() {
        hourCycle("24")
        assertEquals("09:30", context.formatTime(570))
        assertEquals("13:05", context.formatTime(785))
    }

    @Test
    fun timeFollowsTwelveHourSetting() {
        hourCycle("12")
        assertEquals("9:30 AM", context.formatTime(570).plainSpaces())
        assertEquals("1:05 PM", context.formatTime(785).plainSpaces())
    }

    @Test
    fun timeWrapsPastMidnight() {
        hourCycle("24")
        assertEquals("00:00", context.formatTime(1440))
        assertEquals("00:30", context.formatTime(1470))
    }

    @Test
    fun rangeWritesSharedPeriodOnce() {
        hourCycle("12")
        assertEquals("9:00 – 10:30 AM", context.formatTimeRange(540, 630).plainSpaces())
        assertEquals("11:00 AM – 1:00 PM", context.formatTimeRange(660, 780).plainSpaces())
    }

    @Test
    fun rangeFollowsTwentyFourHourSetting() {
        hourCycle("24")
        assertEquals("09:00 – 10:30", context.formatTimeRange(540, 630).plainSpaces())
    }

    @Test
    fun rangeEndingAtOrPastMidnightStaysOnOneDay() {
        hourCycle("24")
        assertEquals("22:00 – 00:00", context.formatTimeRange(1320, 1440).plainSpaces())
        assertEquals("23:00 – 00:15", context.formatTimeRange(1380, 1455).plainSpaces())
    }

    @Test
    fun reversedRangeKeepsBothTimes() {
        hourCycle("24")
        assertEquals("10:00 – 09:00", context.formatTimeRange(600, 540).plainSpaces())
    }

    @Test
    fun hourLabelsCountToTwelveInTwelveHourMode() {
        hourCycle("12")
        assertEquals(listOf("12", "8", "12", "1", "11"), listOf(0, 8, 12, 13, 23).map(context::formatHour))
    }

    @Test
    fun hourLabelsStayBareInTwentyFourHourMode() {
        hourCycle("24")
        assertEquals(listOf("0", "8", "13", "23"), listOf(0, 8, 13, 23).map(context::formatHour))
    }

    @Test
    @Config(qualifiers = "ja-rJP")
    fun hourLabelsStartAtZeroWhereTheLocaleDoes() {
        hourCycle("12")
        assertEquals(listOf("0", "11"), listOf(12, 23).map(context::formatHour))
    }
}
