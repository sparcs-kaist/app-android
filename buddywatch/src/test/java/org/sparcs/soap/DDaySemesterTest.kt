package org.sparcs.soap

import android.app.Application
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.sparcs.soap.complication.DDayComplicationService
import org.sparcs.soap.data.WatchDataStore
import org.sparcs.soap.data.models.Semester
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class DDaySemesterTest {
    @Test
    fun selectedSpringTimetableDoesNotReplaceCurrentFallCountdown() = runBlocking {
        val controller = Robolectric.buildService(DDayComplicationService::class.java).create()
        try {
            val service = controller.get()
            val store = WatchDataStore(service)
            val today = LocalDate.now()
            fun millis(offset: Long) = today.plusDays(offset)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val fall = Json.encodeToString(Semester("2026 Fall", millis(-30), millis(60)))
            val spring = Json.encodeToString(Semester("2026 Spring", millis(-210), millis(-120)))
            store.saveCurrentSemesterJson(fall)
            store.saveTimetableJson("selected spring timetable")
            store.saveSemesterJson(spring)

            val data = service.onComplicationRequest(
                ComplicationRequest(1, ComplicationType.SHORT_TEXT, false)
            ) as ShortTextComplicationData

            assertEquals("D-60", data.text.getTextAt(service.resources, Instant.now()).toString())
            assertEquals(
                "26 Fall",
                data.title!!.getTextAt(service.resources, Instant.now()).toString()
            )
            assertEquals(fall, store.currentSemesterJsonFlow.first())
            assertEquals(spring, store.semesterJsonFlow.first())
            assertEquals("selected spring timetable", store.timetableJsonFlow.first())

            // A timetable change or deletion must leave the calendar semester intact.
            store.clearTimetable()
            assertEquals(fall, store.currentSemesterJsonFlow.first())
        } finally {
            controller.destroy()
        }
    }

    @Test
    fun missingCurrentSemesterDoesNotFallBackToSelectedSemester() = runBlocking {
        val controller = Robolectric.buildService(DDayComplicationService::class.java).create()
        try {
            val service = controller.get()
            val store = WatchDataStore(service)
            store.saveCurrentSemesterJson(null)
            store.saveSemesterJson(Json.encodeToString(Semester("2026 Spring", 1L, 2L)))
            val data = service.onComplicationRequest(
                ComplicationRequest(1, ComplicationType.SHORT_TEXT, false)
            ) as ShortTextComplicationData
            assertEquals(
                "No Sync",
                data.title!!.getTextAt(service.resources, Instant.now()).toString()
            )
        } finally {
            controller.destroy()
        }
    }
}
